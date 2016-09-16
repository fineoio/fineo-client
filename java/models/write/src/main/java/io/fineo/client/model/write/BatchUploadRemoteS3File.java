package io.fineo.client.model.write;


public class BatchUploadRemoteS3File {
  // Remote S3 file to batch read. Format: s3://<bucket>/<file key>
  private String filePath;

  public String getFilePath() {
    return filePath;
  }

  public BatchUploadRemoteS3File setFilePath(String filePath) {
    this.filePath = filePath;
    return this;
  }
}
