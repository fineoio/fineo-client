package io.fineo.client.tools.option;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.beust.jcommander.Parameter;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CredentialsOption {

  private static final Logger LOG = LoggerFactory.getLogger(CredentialsOption.class);

  private AWSCredentialsProvider credentials;

  @Parameter(names = "--profile-name",
             description = "Name of the profile to use when loading creds")
  public String profileName;

  @Parameter(names = "--static-key", description = "Key for the credentials")
  public String staticKey;

  @Parameter(names = "--static-secret", description = "Key for the credentials")
  public String staticSecret;

  @Parameter(names = "--credentials-file", description = "YAML file for the credentials")
  public String credentialsFile;

  @Parameter(names = "--environment-credentials",
             description = "Load credentials from the standard AWS environment keys")
  public boolean envCredentials;

  public AWSCredentialsProvider get() throws FileNotFoundException {
    if (this.credentials == null) {
      credentials = getCredentials();
    }
    return this.credentials;
  }

  private AWSCredentialsProvider getCredentials() throws FileNotFoundException {
    String credentialType = null;
    // load the yaml credentials file into static credentials
    List<AWSCredentialsProvider> providers = new ArrayList<>();
    if (credentialsFile != null) {
      Yaml yaml = new Yaml();
      FileInputStream stream = new FileInputStream(credentialsFile);
      Map map = (Map) yaml.load(stream);
      String key = (String) map.get("access_key_id");
      String value = (String) map.get("secret_access_key");
      providers.add(new AWSStaticCredentialsProvider(new BasicAWSCredentials(key, value)));
    }

    if (profileName != null) {
      providers.add(new ProfileCredentialsProvider(profileName));
    }
    if (staticKey != null && staticSecret != null) {
      providers.add(new AWSStaticCredentialsProvider(new BasicAWSCredentials(staticKey,
        staticSecret)));
    }
    if (envCredentials) {
      providers.add(new EnvironmentVariableCredentialsProvider());
    }

    Preconditions.checkArgument(providers.size() > 0, "No valid credentials provided!");
    return new AWSCredentialsProviderChain(providers);
  }
}
