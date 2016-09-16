package io.fineo.client.model.write;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Information about a record's success. This can quickly be checked with {@link #failed()}. The
 * cause of the error can be found from the other field. If there is no error, the other fields
 * are empty.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Record {

  private String errorCode;
  private String errorMessage;

  @JsonProperty("ErrorCode")
  public String getErrorCode() {
    return errorCode;
  }

  @JsonProperty("ErrorCode")
  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }

  @JsonProperty("ErrorMessage")
  public String getErrorMessage() {
    return errorMessage;
  }

  @JsonProperty("ErrorMessage")
  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  /**
   * @return <tt>true</tt> if the record wasn't added because of an error
   */
  @JsonIgnore
  public boolean failed() {
    return this.errorCode != null;
  }

  @Override
  public String toString() {
    return "Record{" +
           "errorCode='" + errorCode + '\'' +
           ", errorMessage='" + errorMessage + '\'' +
           '}';
  }
}
