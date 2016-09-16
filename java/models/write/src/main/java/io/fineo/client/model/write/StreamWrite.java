package io.fineo.client.model.write;

import io.fineo.client.Op;
import io.fineo.client.model.Empty;
import io.netty.util.concurrent.CompleteFuture;

public interface StreamWrite extends AutoCloseable {

  @Op(method = "PUT", path = "/stream/events")
  <T extends SingleStreamEventBase> StreamRecordsResponse write(T[] events);

  @Op(method = "PUT", path = "/stream/events")
  <T extends SingleStreamEventBase> CompleteFuture<StreamRecordsResponse> writeAsync(T[] events);

  @Op(method = "PUT", path = "/stream/event")
  <T extends SingleStreamEventBase> Empty writeEvent(T event);

  @Op(method = "PUT", path = "/stream/event")
  <T extends SingleStreamEventBase> CompleteFuture<Empty> writeEventAsync(T events);
}
