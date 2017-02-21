package io.fineo.client;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.mockito.Matchers.eq;

public class TestApiAwsClient {

  public static final String SIGNING_TYPE_PREFIX = "AWS4-HMAC-SHA256";
  public static final String CREDENTIAL_REGEX =
    "accesskey/20[0-9]{6}/us-east-1/execute-api/aws4_request";
  public static final String SIGNED_HEADERS =
    "accept;cache-control;content-length;content-type;host;x-amz-date;x-api-key";

  @Test
  public void testGeneratedRequestNoSig() throws Exception {
    String url = "https://api.fineo.io";
    BoundRequestBuilder builder = verifyPost(url);
    Mockito.verifyNoMoreInteractions(builder);
  }

  @Test
  public void testGeneratedRequestNoSigWithPathInUrl() throws Exception {
    String url = "https://api.fineo.io/prefix";
    BoundRequestBuilder builder = verifyPost(url);
    Mockito.verifyNoMoreInteractions(builder);
  }

  @Test
  public void testGeneratedResponseWithSigning() throws Exception {
    String url = "https://api.fineo.io/prefix";
    String apikey = "apikey123";
    verifySignedRequest(apikey, url);
  }

  private static void verifySignedRequest(String apikey, String url)
    throws MalformedURLException, URISyntaxException {
    URL uUrl = new URL(url);
    String host = uUrl.getHost();
    AWSStaticCredentialsProvider provider = new AWSStaticCredentialsProvider(new
      BasicAWSCredentials("accesskey", "secretkey"));
    BoundRequestBuilder builder = verifyPost(url, (client) -> {
      client.setCredentials(provider);
      client.setApiKey(apikey);
    });
    Mockito.verify(builder).addHeader(eq("Authorization"), Mockito.argThat(
      new BaseMatcher<String>() {
        private String error;

        @Override
        public boolean matches(Object item) {
          if (item instanceof String) {
            String s = (String) item;
            error = "Authorization does not start with " + SIGNING_TYPE_PREFIX;
            if (s.startsWith(SIGNING_TYPE_PREFIX)) {
              s = s.substring(SIGNING_TYPE_PREFIX.length() + 1);
              Map<String, String> parts = new HashMap<>();
              for (String part : s.split(", ")) {
                String[] kv = part.split("=");
                parts.put(kv[0], kv[1]);
              }
              String credential = parts.get("Credential");
              error = "Authorization Credential does not match regex: " + CREDENTIAL_REGEX;
              if (credential.matches(CREDENTIAL_REGEX)) {
                String headers = parts.get("SignedHeaders");
                error = "Authorization SignedHeaders does not match: " + SIGNED_HEADERS;
                if (headers.equals(SIGNED_HEADERS)) {
                  error = "Authorization missing signature!";
                  if (parts.get("Signature") != null) {
                    return true;
                  }
                }
              }
            }
          }
          return false;
        }

        @Override
        public void describeTo(Description description) {
          description.appendText(error);
        }
      }));
    Mockito.verify(builder).addHeader(eq("X-Amz-Date"), Mockito.matches("20[0-9]{6}T[0-9]{6}Z"));
    Mockito.verify(builder).addHeader("Cache-Control", "no-cache");
    Mockito.verify(builder).addHeader("Accept", "application/json");
    Mockito.verify(builder).addHeader("x-api-key", apikey);
    Mockito.verify(builder).addHeader("Host", host);
    Mockito.verify(builder).addHeader("Content-Length", "1");
    Mockito.verify(builder).addHeader("Content-Type", "application/json");
    Mockito.verify(builder).setQueryParams(Mockito.anyMap());
    Mockito.verifyNoMoreInteractions(builder);
  }

  private static BoundRequestBuilder verifyPost(String url)
    throws MalformedURLException, URISyntaxException {
    return verifyPost(url, (a) -> {
    });
  }

  private static BoundRequestBuilder verifyPost(String url, Consumer<ApiAwsClient> updater)
    throws MalformedURLException, URISyntaxException {
    AsyncHttpClient http = Mockito.mock(AsyncHttpClient.class);
    BoundRequestBuilder builder = Mockito.mock(BoundRequestBuilder.class);
    Mockito.when(http.preparePost(eq(url + "/env/path"))).thenReturn(builder);

    ApiAwsClient client = new ApiAwsClient(new URL(url), "/env", http);
    updater.accept(client);
    byte[] data = new byte[]{0};
    client.post("path", data);

    Mockito.verify(builder).setBody(data);
    Mockito.verify(builder).execute();
    return builder;
  }
}
