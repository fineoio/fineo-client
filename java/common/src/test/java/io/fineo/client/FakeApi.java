package io.fineo.client;

public interface FakeApi extends AutoCloseable {

  @Op(method = "PUT")
  void doWork();
}
