package io.fineo.client.auth;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class CognitoClientProperties {

  public static final String IDENTITY_POOL_ID = "us-east-1:f0f95abf-97ad-454b-8e3c-6e905d68f6c9";
  public static final String USER_POOL_ID = "us-east-1_nK9lmbtmz";
  public static final String CLIENT_ID = "cjru4j9l96cipbpeugfpq8io8";
  public static final String SECRET;

  static {
    // first try getting from the environment
    String secret = System.getProperty("fineo.client.internal.secret");
    if (secret == null) {
      // or try reading from properties
      String file = "fineo-client.internal";
      InputStream input = CognitoClientProperties.class.getClassLoader().getResourceAsStream(file);
      if(input == null){
        throw new IllegalStateException("Could not load properties file: " + file);
      }
      Properties props = new Properties();
      try {
        props.load(input);
        secret = props.getProperty("app.secret");
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    SECRET = secret;
  }
}
