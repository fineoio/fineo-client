package io.fineo.client;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.io.IOException;

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
}
