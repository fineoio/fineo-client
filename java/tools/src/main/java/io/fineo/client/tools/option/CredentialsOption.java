package io.fineo.client.tools.option;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.beust.jcommander.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

public class CredentialsOption {

  private static final Logger LOG = LoggerFactory.getLogger(CredentialsOption.class);

  private AWSCredentialsProvider credentials;

  @Parameter(names = "--credential-type", description = "profile, static")
  public String credentialType;

  @Parameter(names = "--profile-name",
             description = "Name of the profile to use when loading creds")
  public String profileName;

  @Parameter(names = "--static-key", description = "Key for the credentials")
  public String staticKey;

  @Parameter(names = "--static-secret", description = "Key for the credentials")
  public String staticSecret;

  @Parameter(names = "--credentials-file", description = "YAML file for the credentials")
  public String credentialsFile;

  public AWSCredentialsProvider get() throws FileNotFoundException {
    if (this.credentials == null) {
      credentials = getCredentials();
    }
    return this.credentials;
  }

  private AWSCredentialsProvider getCredentials() throws FileNotFoundException {
    // load the yaml credentials file into static credentials
    if (credentialsFile != null) {
      Yaml yaml = new Yaml();
      FileInputStream stream = new FileInputStream(credentialsFile);
      Map map = (Map) yaml.load(stream);
      credentialType = "static";
      staticKey = (String) map.get("access_key_id");
      staticSecret = (String) map.get("secret_access_key");
    }

    switch (credentialType) {
      case "profile":
        LOG.info("Using profile credentials  - [{}]", profileName);
        return new ProfileCredentialsProvider(profileName);
      case "static":
        LOG.info("Using static credentials  - [{}]", staticKey);
        return new AWSStaticCredentialsProvider(new BasicAWSCredentials(staticKey, staticSecret));
      default:
        throw new IllegalArgumentException("No valid credentials provided!");
    }
  }
}
