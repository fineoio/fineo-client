package io.fineo.read.jdbc;

import org.apache.calcite.avatica.BuiltInConnectionProperty;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class TestAuthenticationUtil {

  @Test
  public void testReadsPropertiesUserPasswordBeforeUsingAWS() throws Exception {
    Properties props = new Properties();
    props.setProperty("user", "user1");
    props.setProperty("password", "password1");
    AuthenticationUtil.setupAuthentication(props);

    assertPassword(props, props.getProperty("user"),  props.getProperty("password"));
  }

  @Test
  public void testFallBackToProvider() throws Exception {
    Properties props = new Properties();
    props.setProperty("authentication", "static");
    props.setProperty(FineoConnectionProperties.AWS_KEY.camelName(), "access_key");
    props.setProperty(FineoConnectionProperties.AWS_SECRET.camelName(), "secret_key");

    AuthenticationUtil.setupAuthentication(props);
    assertPassword(props, "access_key", "secret_key");

    props.setProperty("user", "");
    AuthenticationUtil.setupAuthentication(props);
    assertPassword(props, "access_key", "secret_key");

    props.setProperty("password", "");
    AuthenticationUtil.setupAuthentication(props);
    assertPassword(props, "access_key", "secret_key");
  }

  private void assertPassword(Properties props, String user, String password) {
    assertEquals(props.get(BuiltInConnectionProperty.AVATICA_USER.camelName()), user);
    assertEquals(props.get(BuiltInConnectionProperty.AVATICA_PASSWORD.camelName()), password);
  }
}
