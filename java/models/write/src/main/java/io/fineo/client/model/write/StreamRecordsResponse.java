package io.fineo.client.model.write;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Information about upload the requested records. You can quickly check that all records
 * succeeded if {@link #getFailedRecordCount()} returns <b>zero</b>. If the result is non-zero, some
 * records did not get uploaded and should be retried.
 */
public class StreamRecordsResponse {
  private int failedRecordCount;
  private Record[] records;

  public int getFailedRecordCount() {
    return failedRecordCount;
  }

  public void setFailedRecordCount(int failedRecordCount) {
    this.failedRecordCount = failedRecordCount;
  }

  /**
   * An array of successfully and unsuccessfully processed record results, correlated with the
   * request by natural ordering.
   *
   * @return information about each record's success/failure
   */
  public Record[] getRecords() {
    return records;
  }

  public void setRecords(Record[] records) {
    this.records = records;
  }
}
