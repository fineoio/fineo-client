package io.fineo.client;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.DefaultRequest;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.http.HttpMethodName;
import io.fineo.client.aws.Slf4jLogFactory;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Response;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.Function;

public class ApiAwsClient implements AutoCloseable {
  static {
    Slf4jLogFactory.enable();
  }

  public static final byte[] EMPTY_BYTES = new byte[0];
  private final URI baseUri;
  private final URL url;
  private AWSCredentialsProvider credentials;
  private String apiKey;
  private final DefaultAsyncHttpClient client;

  public ApiAwsClient(URL url, String envPrefix, ClientConfiguration conf)
    throws URISyntaxException, MalformedURLException {
    this.url = new URL(url, envPrefix);
    this.baseUri = url.toURI();
    this.client = new DefaultAsyncHttpClient(conf.build());
  }

  public Future<Response> post(String path, byte[] data)
    throws URISyntaxException, MalformedURLException {
    return prepare(client::preparePost, path, data, HttpMethodName.POST).execute();
  }

  public Future<Response> put(String path, byte[] data)
    throws URISyntaxException, MalformedURLException {
    return prepare(client::preparePut, path, data, HttpMethodName.PUT).execute();
  }

  // get doesn't have a body
  public Future<Response> get(String path, Map<String, List<String>> parameters) throws
    URISyntaxException, MalformedURLException {
    return prepare(client::prepareGet, path, null, HttpMethodName.GET, parameters).execute();
  }

  public Future<Response> patch(String path, byte[] data)
    throws URISyntaxException, MalformedURLException {
    return prepare(client::preparePatch, path, data, HttpMethodName.PATCH).execute();
  }

  public Future<Response> delete(String path, byte[] data)
    throws URISyntaxException, MalformedURLException {
    return prepare(client::prepareDelete, path, data, HttpMethodName.DELETE).execute();
  }

  private BoundRequestBuilder prepare(Function<String, BoundRequestBuilder> func, String relative,
    byte[] data, HttpMethodName method)
    throws MalformedURLException {
    return prepare(func, relative, data, method, Collections.emptyMap());
  }

  private BoundRequestBuilder prepare(Function<String, BoundRequestBuilder> func, String relative,
    byte[] data, HttpMethodName method, Map<String, List<String>> parameters)
    throws MalformedURLException {
    String path = new File(url.getPath(), relative).toString();
    String url = new URL(this.url, path).toExternalForm();
    BoundRequestBuilder request = func.apply(url);
    if (data != null) {
      request.setBody(data);
    }

    if (credentials != null) {
      DefaultRequest<AmazonWebServiceRequest> awsReq = new DefaultRequest("execute-api");

      String contentLength = "";
      if (data != null) {
        awsReq.setContent(new ByteArrayInputStream(data));
        contentLength = Integer.toString(data.length);
        awsReq.addHeader("Content-Length", contentLength);
        awsReq.addHeader("Content-Type", "application/json");
      }

      if (apiKey != null) {
        awsReq.addHeader("x-api-key", apiKey);
      }

      awsReq.addHeader("Cache-Control", "no-cache");
      awsReq.addHeader("Accept", "application/json");
      awsReq.setHttpMethod(method);
      awsReq.setEndpoint(baseUri);
      awsReq.setResourcePath(path);
      awsReq.setParameters(parameters);

      AWS4Signer signer = new AWS4Signer();
      signer.setServiceName("execute-api");
      signer.setRegionName("us-east-1");
      signer.sign(awsReq, credentials.getCredentials());

      for (Map.Entry<String, String> header : awsReq.getHeaders().entrySet()) {
        request.addHeader(header.getKey(), header.getValue());
      }

      request.setQueryParams(awsReq.getParameters());
      return request;
    }
    return request;
  }

  public void setCredentials(AWSCredentialsProvider credentials) {
    this.credentials = credentials;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public void close() {
    this.client.close();
  }
}
