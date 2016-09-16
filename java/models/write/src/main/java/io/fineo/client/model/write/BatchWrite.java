package io.fineo.client.model.write;

import io.fineo.client.Op;
import io.fineo.client.Parameter;
import io.fineo.client.model.Empty;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

public interface BatchWrite extends AutoCloseable {

  @Op(method = "PUT", path = "/batch/upload/data/{file}")
  <T extends SingleStreamEventBase> Empty write(
    @Parameter(name = "file", type = Parameter.Type.PATH, nullStrategy = Parameter.Strategy.RANDOM)
      String asFile, T[] events);

  @Op(method = "PUT", path = "/batch/upload/data/{file}")
  <T extends SingleStreamEventBase> CompletableFuture<Empty> writeAsync(
    @Parameter(name = "file", type = Parameter.Type.PATH, nullStrategy = Parameter.Strategy.RANDOM)
      String asFile, T[] events);

  @Op(method = "POST", path = "/batch/upload/file")
  Empty uploadS3File(BatchUploadRemoteS3File file);

  @Op(method = "POST", path = "/batch/upload/file")
  CompletableFuture<Empty> uploadS3FileAsync(BatchUploadRemoteS3File file);

  @Op(method = "GET", path = "/batch/status/{file}")
  Empty status(@Parameter(name = "file", type = Parameter.Type.PATH) URI s3File);

  @Op(method = "GET", path = "/batch/status/{file}")
  CompletableFuture<Empty> statusAsync(@Parameter(name = "file", type = Parameter.Type.PATH) URI
    s3File);
}
