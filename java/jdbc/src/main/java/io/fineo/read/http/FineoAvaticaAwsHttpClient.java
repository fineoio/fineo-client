package io.fineo.read.http;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import io.fineo.client.ApiAwsClient;
import io.fineo.client.ClientConfiguration;
import io.fineo.read.AwsApiGatewayBytesTranslator;
import io.fineo.read.jdbc.ConnectionStringBuilder;
import io.fineo.read.jdbc.FineoConnectionProperties;
import org.apache.calcite.avatica.remote.AuthenticationType;
import org.apache.calcite.avatica.remote.AvaticaHttpClient;
import org.apache.calcite.avatica.remote.UsernamePasswordAuthenticateable;
import org.asynchttpclient.Response;

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
    String prefix = properties.computeIfAbsent("fineo.internal.test.api-prefix", k -> "/prod");
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
    try {
      Response response = client.post("/", request).get();
      // not successful, throw it as an error
      if (error(response)) {
        throw asClientException(response, "AVATICA");
      }
      return translator.decode(response.getResponseBodyAsBytes());
    } catch (InterruptedException | ExecutionException | URISyntaxException | IOException e) {
      throw new RuntimeException(e);
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
}
