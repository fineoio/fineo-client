package io.fineo.read.jdbc;

import io.fineo.client.FineoApiClientException;
import io.fineo.read.Driver;
import io.fineo.read.http.FineoAvaticaAwsHttpClient;
import org.apache.calcite.avatica.AvaticaConnection;
import org.apache.calcite.avatica.ConnectionConfigImpl;
import org.apache.calcite.avatica.remote.RemoteProtobufService;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.amazonaws.SDKGlobalConfiguration.ACCESS_KEY_SYSTEM_PROPERTY;
import static com.amazonaws.SDKGlobalConfiguration.SECRET_KEY_SYSTEM_PROPERTY;
import static java.lang.String.format;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

public class TestDriver {

  static {
    DriverForTesting.loadForTesting();
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @After
  public void cleanupDriver() throws SQLException {
    DriverForTesting driver = (DriverForTesting) DriverManager.getDriver("jdbc:fineo-test");
    driver.getConnections().clear();
  }

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

  @Test
  public void testSetPropertiesInClient() throws Exception {
    // ensure that we an AWS key/secret set properties set
    System.setProperty(ACCESS_KEY_SYSTEM_PROPERTY, "AKIAIUHLZLB7VTXUAX7A");
    System.setProperty(SECRET_KEY_SYSTEM_PROPERTY, "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");
    DriverForTesting driver = (DriverForTesting) DriverManager.getDriver("jdbc:fineo-test:");
    String url = "http://www.google.com";
    try {
      DriverManager.getConnection(format("jdbc:fineo-test:url=%s;api_key=1234", url));
      fail("Should not be able to connect");
    } catch (FineoApiClientException e) {
      //expected
    }
    List<Connection> connections = driver.getConnections();
    assertEquals(1, connections.size());
    AvaticaConnection conn = (AvaticaConnection) connections.get(0);
    RemoteProtobufService service = (RemoteProtobufService) conn.getService();
    // use reflection to really get in there
    Field field = RemoteProtobufService.class.getDeclaredField("client");
    field.setAccessible(true);
    FineoAvaticaAwsHttpClient value = (FineoAvaticaAwsHttpClient) field.get(service);
    Map<String, String> props = value.getPropertiesForTesting();
    assertEquals("1234", props.get(FineoConnectionProperties.API_KEY.camelName()));
  }

  @Test
  public void testParseUrlWithoutExplicitParameter() throws Exception {
    String url = "//the-fineo-url.at.something?api_key=1234";
    Properties info = parseUrl(url);
    ConnectionConfigImpl config = new ConnectionConfigImpl(info);
    assertEquals("https:" + url, config.url());
    assertEquals("1234", info.get("api_key"));
  }

  @Test
  public void testParseUrlWithSlash() throws Exception {
    String url = "//the-fineo-url.at.something/?api_key=1234";
    Properties info = parseUrl(url);
    ConnectionConfigImpl config = new ConnectionConfigImpl(info);
    assertEquals("https://the-fineo-url.at.something?api_key=1234", config.url());
  }

  @Test
  public void testDriverSuffix() throws Exception {
    assertEquals(Driver.class, DriverManager.getDriver("jdbc:fineo").getClass());
    assertNotEquals(Driver.class, DriverManager.getDriver("jdbc:fineo-test").getClass());
    assertEquals(Driver.class, DriverManager.getDriver("jdbc:fineo:").getClass());
    assertEquals(Driver.class, DriverManager.getDriver("jdbc:fineo:api_key=1234").getClass());
  }

  private Properties parseUrl(String url) throws Exception {
    FakeFactoryDriver driver = new FakeFactoryDriver();
    Properties props = new Properties();
    return driver.prepareProperties(format("jdbc:fineo:%s", url), props);
  }

  private static class FakeFactoryDriver extends io.fineo.read.Driver {

    public FakeFactoryDriver() throws ClassNotFoundException {
      super();
    }

    @Override
    public Properties prepareProperties(String url, Properties info)
      throws SQLException, IOException {
      return super.prepareProperties(url, info);
    }
  }
}
