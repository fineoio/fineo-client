package io.fineo.read.jdbc;

import io.fineo.client.FineoApiClientException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.DriverManager;

import static com.amazonaws.SDKGlobalConfiguration.ACCESS_KEY_SYSTEM_PROPERTY;
import static com.amazonaws.SDKGlobalConfiguration.SECRET_KEY_SYSTEM_PROPERTY;
import static java.lang.String.format;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.fail;

public class TestDriver {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  /**
   * Catch for legacy code that didn't get handle the case where we fail to connect to the
   * service when initializing the connection. It would leave an open connection in the map,
   * which left a dangling non-daemon thread pool.
   *
   * @throws Exception on failure
   */
  @Test
  public void testCloseClientConnectionOnServiceFailure() throws Exception {
    // ensure that we an AWS key/secret set properties set
    System.setProperty(ACCESS_KEY_SYSTEM_PROPERTY, "AKIAIUHLZLB7VTXUAX7A");
    System.setProperty(SECRET_KEY_SYSTEM_PROPERTY, "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");

    DriverForTesting.loadForTesting();
    DriverForTesting driver = (DriverForTesting) DriverManager.getDriver("jdbc:fineo-test:");
    String url = "http://www.google.com";
    try {
      DriverManager.getConnection(format("jdbc:fineo-test:url=%s;api_key=1234", url));
      fail("Should not be able to connect");
    } catch (FineoApiClientException e) {
      //expected
    }
    assertEquals("Still some open connections", 0, driver.getConnectionMap().size());
  }
}
