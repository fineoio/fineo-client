package io.fineo.read;

import com.google.common.base.Preconditions;
import io.fineo.read.http.FineoAvaticaAwsHttpClient;
import io.fineo.read.jdbc.ConnectionStringBuilder;
import io.fineo.read.jdbc.FineoConnectionProperties;
import org.apache.calcite.avatica.AvaticaConnection;
import org.apache.calcite.avatica.BuiltInConnectionProperty;
import org.apache.calcite.avatica.ConnectStringParser;
import org.apache.calcite.avatica.ConnectionConfig;
import org.apache.calcite.avatica.ConnectionProperty;
import org.apache.calcite.avatica.DriverVersion;
import org.apache.calcite.avatica.Handler;
import org.apache.calcite.avatica.HandlerImpl;
import org.apache.calcite.avatica.remote.AvaticaHttpClient;
import org.apache.calcite.avatica.remote.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static io.fineo.read.jdbc.AuthenticationUtil.setupAuthentication;
import static io.fineo.read.jdbc.FineoConnectionProperties.API_KEY;
import static org.apache.calcite.avatica.BuiltInConnectionProperty.HTTP_CLIENT_IMPL;
import static org.apache.calcite.avatica.BuiltInConnectionProperty.SERIALIZATION;
import static org.apache.calcite.avatica.remote.Driver.Serialization.PROTOBUF;

public class Driver extends org.apache.calcite.avatica.remote.Driver {

  private static final Logger LOG = LoggerFactory.getLogger(Driver.class);
  public static final String CONNECT_PREFIX = "jdbc:fineo:";

  static {
    try {
      new Driver().register();
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * A bit cleaner than calling Class.forName(...)
   */
  public static boolean load() {
    return true;
  }

  protected final Map<String, ConnectionInfo> open = new HashMap<>();

  public Driver() throws ClassNotFoundException {
    super();
    Class.forName(org.apache.calcite.avatica.remote.Driver.class.getName());
  }

  @Override
  protected DriverVersion createDriverVersion() {
    return DriverVersion.load(
      org.apache.calcite.avatica.remote.Driver.class,
      "io-fineo-jdbc.properties",
      "Fineo JDBC Driver",
      "unknown version",
      "Fineo",
      "unknown version");
  }

  @Override
  protected String getConnectStringPrefix() {
    return CONNECT_PREFIX;
  }

  @Override
  protected Collection<ConnectionProperty> getConnectionProperties() {
    final List<ConnectionProperty> list = new ArrayList<ConnectionProperty>();
    Collections.addAll(list, FineoConnectionProperties.values());
    return list;
  }

  @Override
  public Connection connect(String url, Properties baseInfo) throws SQLException {
    if (!acceptsURL(url)) {
      return null;
    }
    try {
      // do the same parsing as in UnregisteredDriver#connect(...). We so this here so we can
      // generate a URL that contains all the necessary properties, allowing them to get passed
      // to our custom client. It also allows us to manage closing the client pool if we don't
      // finish initializing correctly
      final String prefix = getConnectStringPrefix();
      assert url.startsWith(prefix);
      final String urlSuffix = url.substring(prefix.length());
      final Properties info2 = ConnectStringParser.parse(urlSuffix, baseInfo);
      updateProperties(info2);

      // Unregistered Driver stuff
      final AvaticaConnection connection = factory.newConnection(this, factory, url, info2);
      handler.onConnectionInit(connection);

      try {
        // avatica.Driver stuff
        Service service = connection.getService();

        // super.connect(...) should be creating a service and setting it in the AvaticaConnection
        assert null != service;

        service.apply(
          new Service.OpenConnectionRequest(connection.id,
            Service.OpenConnectionRequest.serializeProperties(info2)));
      } catch (RuntimeException e) {
        // can happen if we have a bad connection on the server side.
        try {
          connection.close();
        } catch (SQLException ce) {
          LOG.error("Failed to correctly close the connection!", ce);
          // make sure that we definitely close the thread pool
          close(connection);
        }
        throw e;
      }
      return connection;
    } catch (IOException e) {
      throw new SQLException("Unexpected exception while obtaining connection!");
    }
  }

  /**
   * Update the properties to fix inside the avatica framework. Does things like:
   * <ol>
   * <li>Translate aws auth into avatica auth</li>
   * <li>Update url to include things like the Api Key
   * <ul>
   * <li>The client is created with the URL from the properties (url=), not the overall
   * connection URL, so we have to update the url here
   * </li>
   * </ul>
   * </li>
   * </ol>
   */
  private void updateProperties(Properties info) throws IOException {
    // ensure we use our factory to create our client
    info.put(HTTP_CLIENT_IMPL.camelName(), FineoAvaticaAwsHttpClient.class.getName());
    // yup, always use protobuf
    info.put(SERIALIZATION.camelName(), PROTOBUF.toString());
    setupAuthentication(info);

    // properties that are passed through the connection string
    ConnectionStringBuilder sb = new ConnectionStringBuilder(
      BuiltInConnectionProperty.URL.wrap(info).getString("=== No URL Specified ==="));
    String key = Preconditions
      .checkNotNull(API_KEY.wrap(info).getString(), "Must specify the Fineo API Key via %s",
        API_KEY.camelName());
    sb.with(API_KEY, info);

    // API KEY is also the company key, and we need that in the connection properties on the
    // server side
    info.put(FineoJdbcProperties.COMPANY_KEY_PROPERTY, key);
    setupClientProperties(info, sb);

    // testing override
    String testPrefix = info.getProperty("fineo.internal.test.api-prefix");
    if (testPrefix != null) {
      sb.with("fineo.internal.test.api-prefix", testPrefix);
    }
    String url = sb.build();
    info.setProperty("url", url);
  }

  /**
   * pull out the client/connection properties into the system, since we can't get an instance
   * of the properties in the client proper... yeah, come on avatica
   */
  private void setupClientProperties(Properties info, ConnectionStringBuilder sb) {
    sb.withInt(FineoConnectionProperties.CLIENT_INIT_TIMEOUT, info)
      .withInt(FineoConnectionProperties.CLIENT_MAX_CONNECTIONS, info)
      .withInt(FineoConnectionProperties.CLIENT_REQUEST_TIMEOUT, info)
      .withInt(FineoConnectionProperties.CLIENT_MAX_ERROR_RETRY, info);
  }

  // on close we want to close thread-pool handle in the client, so we keep around a reference to
  // all the connections we have open (assumed to be relatively small) and then try to find the
  // connection when we get a closed notification

  @Override
  protected AvaticaHttpClient getHttpClient(AvaticaConnection connection, ConnectionConfig config) {
    AvaticaHttpClient client = super.getHttpClient(connection, config);
    open.put(connection.id, new ConnectionInfo(connection, (FineoAvaticaAwsHttpClient) client));
    return client;
  }

  @Override
  protected Handler createHandler() {
    return new HandlerImpl() {

      @Override
      public void onConnectionClose(AvaticaConnection connection) {
        Driver.this.close(connection);
      }
    };
  }

  private void close(AvaticaConnection connection) {
    ConnectionInfo info = open.remove(connection.id);
    if (info == null) {
      LOG.error("Could not find connection: %s on driver!", connection.id);
      return;
    }

    info.client.close();
  }

  private class ConnectionInfo {
    private final AvaticaConnection conn;
    private final FineoAvaticaAwsHttpClient client;

    public ConnectionInfo(AvaticaConnection connection,
      FineoAvaticaAwsHttpClient client) {
      this.conn = connection;
      this.client = client;
    }
  }
}
