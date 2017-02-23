package io.fineo.client.model.write;

import io.fineo.client.Api;
import io.fineo.client.Op;
import io.fineo.client.model.Empty;

import java.util.concurrent.CompletableFuture;

@Api("https://api.fineo.io/stream")
public interface StreamWrite extends AutoCloseable {

  @Op(method = "PUT", path = "/stream/events")
  <T> StreamRecordsResponse write(T[] events);

  @Op(method = "PUT", path = "/stream/events")
  <T> CompletableFuture<StreamRecordsResponse> writeAsync(T[] events);

  @Op(method = "PUT", path = "/stream/event")
  <T> Empty writeEvent(T event);

  @Op(method = "PUT", path = "/stream/event")
  <T> CompletableFuture<Empty> writeEventAsync(T events);
}
