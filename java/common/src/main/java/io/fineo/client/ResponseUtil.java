package io.fineo.client;

import com.amazonaws.util.IOUtils;
import org.asynchttpclient.Response;

import java.io.IOException;
import java.io.InputStream;

public class ResponseUtil {

  private ResponseUtil(){}

  /**
   * Check to see if the response is an 'error'
   * @param response
   * @return <tt>true</tt> if it is an error, false otherwise
   */
  public static boolean error(Response response) {
    int code = response.getStatusCode();
    return !(code >= 200 && code < 300);
  }

  public static FineoApiClientException asClientException(Response response, String method)
    throws IOException {
    InputStream content = response.getResponseBodyAsStream();
    String error = content == null ? "" : IOUtils.toString(content);
    StringBuffer sb = new StringBuffer("[");
    sb.append(response.getStatusCode());
    sb.append("] ");
    sb.append(response.getStatusText());
    sb.append(": \n");
    sb.append(error);
    FineoApiClientException e = new FineoApiClientException(sb.toString());
    e.setMethod(method);
    e.setStatusCode(response.getStatusCode());
    String requestId = response.getHeaders().get("x-amzn-RequestId");
    if (requestId != null) {
      e.setRequestId(requestId);
    }
    return e;
  }
}
