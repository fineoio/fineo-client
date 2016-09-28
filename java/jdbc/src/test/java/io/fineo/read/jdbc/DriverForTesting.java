package io.fineo.read.jdbc;

import io.fineo.read.Driver;
import org.apache.calcite.avatica.AvaticaConnection;
import org.apache.calcite.avatica.AvaticaStatement;
import org.apache.calcite.avatica.Handler;
import org.apache.calcite.avatica.HandlerImpl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class DriverForTesting extends Driver {
  static {
    try {
      new DriverForTesting().register();
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private List<Connection> connections = new ArrayList<>();

  public DriverForTesting() throws ClassNotFoundException {
    super();
  }

  @Override
  protected String getConnectStringPrefix() {
    return "jdbc:fineo-test:";
  }

  Map getConnectionMap() {
    return open;
  }

  static boolean loadForTesting() {
    return true;
  }

  @Override
  protected Handler createHandler() {
    Handler delegate = super.createHandler();
    Handler handler = new HandlerImpl() {
      public void onConnectionInit(AvaticaConnection connection) throws SQLException {
        delegate.onConnectionInit(connection);
        connections.add(connection);
      }

      public void onConnectionClose(AvaticaConnection connection) {
        delegate.onConnectionClose(connection);
      }

      public void onStatementExecute(AvaticaStatement statement,
        ResultSink resultSink) {
        delegate.onStatementExecute(statement, resultSink);
      }

      public void onStatementClose(AvaticaStatement statement) {
        delegate.onStatementClose(statement);
      }
    };
    return handler;
  }

  List<Connection> getConnections() {
    return this.connections;
  }
}
