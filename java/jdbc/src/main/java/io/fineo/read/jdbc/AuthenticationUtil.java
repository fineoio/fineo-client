package io.fineo.read.jdbc;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import org.apache.calcite.avatica.BuiltInConnectionProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class AuthenticationUtil {

  private static final Logger LOG = LoggerFactory.getLogger(AuthenticationUtil.class);
  private static final String AUTH_TYPE_SEPARATOR = "_OR_";

  /**
   * Get the authentication from the Properties. Maybe the user/password are already set, in
   * which case, just use those. Otherwise, try the various AWS authentication methods
   * @param info connection properties to read/update
   */
  public static void setupAuthentication(Properties info) {
    // check to see if username/password set
    String user = info.getProperty("user", null);
    String password = info.getProperty("password", null);
    if (user == null || user.length() == 0 || password == null || password.length() == 0) {
      // load all the places the credentials could be stored
      AWSCredentialsProviderChain chain = loadCredentialChain(info);
      user = chain.getCredentials().getAWSAccessKeyId();
      password = chain.getCredentials().getAWSSecretKey();
    }
    info.setProperty(BuiltInConnectionProperty.AVATICA_USER.camelName(), user);
    info.setProperty(BuiltInConnectionProperty.AVATICA_PASSWORD.camelName(), password);
  }

  private static AWSCredentialsProviderChain loadCredentialChain(Properties info) {
    String authType = FineoConnectionProperties.AUTHENTICATION.wrap(info).getString();
    String[] types = authType.split(AUTH_TYPE_SEPARATOR);
    List<AWSCredentialsProvider> sources = new ArrayList<>();
    if (types == null || types.length == 0) {
      sources.add(new DefaultAWSCredentialsProviderChain());
    }
    for (String type : types) {
      switch (type.toLowerCase()) {
        case "default":
          return new DefaultAWSCredentialsProviderChain();
        case "static":
          String key = FineoConnectionProperties.AWS_KEY.wrap(info).getString();
          String secret = FineoConnectionProperties.AWS_SECRET.wrap(info).getString();
          sources.add(new AWSStaticCredentialsProvider(new BasicAWSCredentials(key, secret)));
          break;
        case "system":
          sources.add(new SystemPropertiesCredentialsProvider());
          break;
        case "env":
          sources.add(new EnvironmentVariableCredentialsProvider());
          break;
        case "profile":
          sources.add(new ProfileCredentialsProvider(FineoConnectionProperties
            .PROFILE_CREDENTIAL_NAME.wrap(info).getString()));
          break;
        default:
          LOG.warn("No authentication provider of type {} supported!", type);
      }
    }

    return new AWSCredentialsProviderChain(sources.toArray(new AWSCredentialsProvider[0]));
  }
}
