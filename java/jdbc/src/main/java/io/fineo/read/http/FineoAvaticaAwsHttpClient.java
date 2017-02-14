package io.fineo.read.http;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.google.common.annotations.VisibleForTesting;
import io.fineo.client.ApiAwsClient;
import io.fineo.client.ClientConfiguration;
import io.fineo.read.AwsApiGatewayBytesTranslator;
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
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static io.fineo.client.ResponseUtil.asClientException;
import static io.fineo.client.ResponseUtil.error;
import static io.fineo.read.jdbc.ConnectionPropertyUtil.setInt;
import static io.fineo.read.jdbc.FineoConnectionProperties.API_KEY;

/**
 * An Avatica client that writes/reads a Fineo AWS endpoint
 */
public class FineoAvaticaAwsHttpClient implements AvaticaHttpClient,
                                                  UsernamePasswordAuthenticateable {

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
        this.credentials =
          new AWSStaticCredentialsProvider(new BasicAWSCredentials(username, password));
    }
  }

  public void close() {
    this.client.close();
  }

  @VisibleForTesting
  public Map<String, String> getPropertiesForTesting() {
    return this.properties;
  }
}
