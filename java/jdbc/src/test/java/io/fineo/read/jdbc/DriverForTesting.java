package io.fineo.read.jdbc;

import io.fineo.read.Driver;

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

  public DriverForTesting() throws ClassNotFoundException {
    super();
  }

  @Override
  protected String getConnectStringPrefix() {
    return "jdbc:fineo-test:";
  }

  public Map getConnectionMap(){
    return open;
  }

  public static boolean loadForTesting() {
    return true;
  }
}
