package io.fineo.client;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;


public class TestGeneratedClient {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testAutoCloseable() throws Exception {
    ApiAwsClient client = Mockito.mock(ApiAwsClient.class);
    try (FakeApi api = FineoClientBuilder
      .build(FakeApi.class, new FineoClientBuilder.ApiClientHandler(client))) {
    }

    Mockito.verify(client).close();
  }

  @Test
  public void testThrowActualCause() throws Exception {
    String message = "Injected exception from mockito instance";
    ApiAwsClient client = Mockito.mock(ApiAwsClient.class);
    Mockito.when(client.put(any(), any())).thenThrow(new RuntimeException(message));
    FakeApi api =
      FineoClientBuilder.build(FakeApi.class, new FineoClientBuilder.ApiClientHandler(client));
    thrown.expect(RuntimeException.class);
    thrown.expectMessage(message);
    api.doWork();
  }

  @Test
  public void testApiEndpoint() throws Exception {
    FineoClientBuilder builder = new FineoClientBuilder()
      .withApiKey("somekey");
    String endpoint = builder.getEndpoint(FakeApi.class);
    assertEquals(FakeApi.API_ENDPOINT, endpoint);
    endpoint = "https://some.other.endpoint";

    builder = builder.withEndpoint(endpoint);
    assertEquals(endpoint, builder.getEndpoint(FakeApi.class));

    assertEquals(endpoint, builder.getEndpoint(FakeApi.SubApi.class));
    builder = builder.withEndpoint(null);
    assertEquals(FakeApi.API_ENDPOINT, builder.getEndpoint(FakeApi.SubApi.class));
  }

}
