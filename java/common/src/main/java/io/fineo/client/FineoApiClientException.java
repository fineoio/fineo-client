package io.fineo.client;

/**
 * Thrown when there is a server-side exception
 */
public class FineoApiClientException extends RuntimeException{
  private int statusCode;
  private String requestId;
  private String method;

  public FineoApiClientException(String message) {
    super(message);
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public String getRequestId() {
    return requestId;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public String getMethod() {
    return method;
  }
}
