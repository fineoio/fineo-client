package io.fineo.read.http;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.internal.StaticCredentialsProvider;
import io.fineo.client.ApiAwsClient;
import io.fineo.client.ClientConfiguration;
import io.fineo.read.AwsApiGatewayBytesTranslator;
import io.fineo.read.jdbc.ConnectionStringBuilder;
import io.fineo.read.jdbc.FineoConnectionProperties;
import org.apache.calcite.avatica.remote.AuthenticationType;
import org.apache.calcite.avatica.remote.AvaticaHttpClient;
import org.apache.calcite.avatica.remote.UsernamePasswordAuthenticateable;
import org.asynchttpclient.Response;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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
  private StaticCredentialsProvider credentials;

  public FineoAvaticaAwsHttpClient(URL url) throws MalformedURLException, URISyntaxException {
    // simplify the url to just the bit we will actually send
    url = (
      url.getPort() == -1 ?
      new URL(url.getProtocol(), url.getHost(), url.getPath()) :
      new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getPath()));
    this.properties = ConnectionStringBuilder.parse(url);
    this.client = new ApiAwsClient(url, "/prod", getConf(this.properties));
    client.setApiKey(properties.get(API_KEY));
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
      return translator.decode(response.getResponseBodyAsBytes());
    } catch (InterruptedException | ExecutionException | URISyntaxException |
      MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void setUsernamePassword(AuthenticationType authType, String username, String password) {
    switch (authType) {
      case BASIC:
      case DIGEST:
        this.credentials =
          new StaticCredentialsProvider(new BasicAWSCredentials(username, password));
    }
  }

  public void close() {
    this.client.close();
  }
}
