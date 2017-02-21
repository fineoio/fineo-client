package io.fineo.read.http;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.google.common.annotations.VisibleForTesting;
import io.fineo.client.ApiAwsClient;
import io.fineo.client.ClientConfiguration;
import io.fineo.read.AwsApiGatewayBytesTranslator;
import io.fineo.read.DriverProperties;
import io.fineo.read.auth.CognitoCachingCredentialsProvider;
import io.fineo.read.auth.cognito.CognitoUser;
import io.fineo.read.auth.cognito.CognitoUserPool;
import io.fineo.read.auth.cognito.CognitoUserSession;
import io.fineo.read.auth.cognito.handle.AuthenticationContinuation;
import io.fineo.read.auth.cognito.handle.AuthenticationDetails;
import io.fineo.read.auth.cognito.handle.AuthenticationHandler;
import io.fineo.read.jdbc.ConnectionStringBuilder;
import io.fineo.read.jdbc.FineoConnectionProperties;
import org.apache.calcite.avatica.remote.AuthenticationType;
import org.apache.calcite.avatica.remote.AvaticaHttpClient;
import org.apache.calcite.avatica.remote.UsernamePasswordAuthenticateable;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import static io.fineo.client.ResponseUtil.asClientException;
import static io.fineo.client.ResponseUtil.error;
import static io.fineo.read.jdbc.ConnectionPropertyUtil.setInt;
import static io.fineo.read.jdbc.FineoConnectionProperties.API_KEY;

/**
 * An Avatica client that writes/reads a Fineo AWS endpoint
 */
public class FineoAvaticaAwsHttpClient
  implements AvaticaHttpClient, UsernamePasswordAuthenticateable, AutoCloseable {

  private static final Logger LOG = LoggerFactory.getLogger(FineoAvaticaAwsHttpClient.class);

  private final AwsApiGatewayBytesTranslator translator = new AwsApiGatewayBytesTranslator();
  private final Map<String, String> properties;
  private final ApiAwsClient client;
  private AWSCredentialsProvider credentials;

  public FineoAvaticaAwsHttpClient(URL url) throws MalformedURLException, URISyntaxException {
    // first, get the properties
    this.properties = ConnectionStringBuilder.parse(url);

    // simplify the url to just the bit we will actually send
    url = (
      url.getPort() == -1 ?
      new URL(url.getProtocol(), url.getHost(), url.getPath()) :
      new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getPath()));
    // create a connection
    String prefix = properties.computeIfAbsent("fineo.internal.test.api-prefix", k -> "/v1");
    this.client = new ApiAwsClient(url, prefix, getConf(this.properties));
    client.setApiKey(properties.get(API_KEY.camelName()));
  }

  private io.fineo.client.ClientConfiguration getConf(Map<String, String> properties) {
    ClientConfiguration conf = new ClientConfiguration();
    setInt(properties, FineoConnectionProperties.CLIENT_MAX_CONNECTIONS,
      prop -> conf.setMaxConnections(prop));
    setInt(properties, FineoConnectionProperties.CLIENT_REQUEST_TIMEOUT,
      prop -> conf.setReadTimeout(prop));
    setInt(properties, FineoConnectionProperties.CLIENT_INIT_TIMEOUT,
      prop -> conf.setConnectTimeout(prop));
    setInt(properties, FineoConnectionProperties.CLIENT_MAX_ERROR_RETRY,
      prop -> conf.setMaxRequestRetry(prop));
    return conf;
  }

  @Override
  public byte[] send(byte[] request) {
    client.setCredentials(this.credentials);
    request = translator.encode(request);
    Response response;
    try {
      response = client.post("/", request).get();
    } catch (InterruptedException | ExecutionException | URISyntaxException | IOException e) {
      throw new RuntimeException(e);
    }
    try {
      // Success and error over 500 (avatica, server-side error) need to unpack the bytes
      // Otherwise, its an AWS error, so we should just unpack it regularly;
      // 504 is special - AWS API Gateway sends that when requests take longer than 30 seconds.
      if (!error(response) ||
          (response.getStatusCode() >= 500 && response.getStatusCode() != 504)) {
        return translator.decode(response.getResponseBodyAsBytes());
      } else {
        throw asClientException(response, "AVATICA");
      }
    } catch (IllegalArgumentException e) {
      LOG.error("Could not process response from server: {}", e.getMessage());
      LOG.error("Raw message content: \n" + response.getResponseBody());
      throw e;
    }
  }

  @Override
  public void setUsernamePassword(AuthenticationType authType, String username, String password) {
    switch (authType) {
      case BASIC:
      case DIGEST:
        // look at the shape of the credentials OR check the config flag
        boolean force = this.properties.getOrDefault("force_auth", "").equals("username_password");
        this.credentials =
          (force || userShape(username, password)) ?
          getUserCredentials(username, password) :
          new AWSStaticCredentialsProvider(new BasicAWSCredentials(username, password));
    }
  }

  private AWSCredentialsProvider getUserCredentials(String username, String password) {
    CognitoUserPool pool = new CognitoUserPool(
      DriverProperties.USER_POOL_ID, DriverProperties.CLIENT_ID, DriverProperties.SECRET);
    CognitoUser user = pool.getUser(username);
      CountDownLatch done = new CountDownLatch(1);
      AtomicReference<CognitoUserSession> sessionRef = new AtomicReference<>();
      AtomicReference<Exception> error = new AtomicReference<>();
      user.getSession(new AuthenticationHandler() {
        @Override
        public void onSuccess(CognitoUserSession userSession, Object newDevice) {
          sessionRef.set(userSession);
          done.countDown();
        }

        @Override
        public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation,
          String UserId) {
          AuthenticationDetails authDetails = new AuthenticationDetails(username, password,
            new HashMap<>());
          authenticationContinuation.setAuthenticationDetails(authDetails);
          authenticationContinuation.continueTask();
        }

        @Override
        public void getMFACode(Object mfaContinuation) {
          throw new IllegalStateException("JDBC does not support MFA based login");
        }

        @Override
        public void authenticationChallenge(Object continuation) {
          throw new IllegalStateException("JDBC does not support authentication challenges");
        }

        @Override
        public void onFailure(Exception exception) {
          error.set(exception);
          done.countDown();
        }
      });
    try {
      done.await();
      Exception e = error.get();
      if (e != null) {
        throw new RuntimeException(e);
      }
      CognitoUserSession session = sessionRef.get();
      CognitoCachingCredentialsProvider credentialsProvider = new
        CognitoCachingCredentialsProvider(DriverProperties.IDENTITY_POOL_ID, Regions.US_EAST_1);
      Map<String, String> logins = new HashMap<>();
      logins.put("cognito-idp.us-east-1.amazonaws.com/" + DriverProperties.USER_POOL_ID, session.getIdToken()
                                                                                                .getJWTToken());
      credentialsProvider.setLogins(logins);
      return credentialsProvider;
    } catch (InterruptedException e) {
      throw new RuntimeException("Interrupted waiting for user credentials!", e);
    }
  }

  private boolean userShape(String username, String password) {
    return username.contains("@");
  }

  @Override
  public void close() throws IOException {
    this.client.close();
  }

  @VisibleForTesting
  public Map<String, String> getPropertiesForTesting() {
    return this.properties;
  }
}
