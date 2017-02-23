package io.fineo.client;

@Api(FakeApi.API_ENDPOINT)
public interface FakeApi extends AutoCloseable {

  String API_ENDPOINT = "https://api.fineo.io/fake-for-testing";

  @Op(method = "PUT")
  void doWork();

  interface SubApi extends FakeApi{
  }
}
