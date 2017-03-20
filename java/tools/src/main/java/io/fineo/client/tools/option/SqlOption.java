package io.fineo.client.tools.option;


import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.google.common.base.Joiner;
import io.fineo.read.Driver;
import io.fineo.read.jdbc.FineoConnectionProperties;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class SqlOption {

  @Parameter(description = "query")
  private List<String> queryParts = new ArrayList<>();

  @Parameter(names = "--sql", description = "File containing the SQL query")
  private String sql;

  @ParametersDelegate
  ApiOption api = new ApiOption();

  public String getUrl() throws FileNotFoundException {
    StringBuffer sb = new StringBuffer(Driver.CONNECT_PREFIX+":");
    boolean first = true;
    if (api.url != null) {
      sb.append("url=");
      sb.append(api.url);
      first = false;
    }

    append(first, sb, FineoConnectionProperties.API_KEY, api.key);
    CredentialsOption creds = api.credentials;
    if (creds.username != null) {
      append(sb, "username", creds.username);
      append(sb, "password", creds.password);
    } else {
      append(sb, FineoConnectionProperties.AUTHENTICATION, "static");
      AWSCredentialsProvider provider = api.credentials.get();
      AWSCredentials credentials = provider.getCredentials();
      append(sb, FineoConnectionProperties.AWS_KEY, credentials.getAWSAccessKeyId());
      append(sb, FineoConnectionProperties.AWS_SECRET, credentials.getAWSSecretKey());
    }

    return sb.toString();
  }

  private void append(boolean first, StringBuffer buff, FineoConnectionProperties property, String
    value) {
    if (first) {
      buff.append(property.camelName());
      buff.append("=");
      buff.append(value);
    } else {
      append(buff, property.camelName(), value);
    }
  }

  private void append(StringBuffer buff, FineoConnectionProperties property, String value) {
    append(buff, property.camelName(), value);
  }

  private void append(StringBuffer buff, String key, String value) {
    buff.append(";");
    buff.append(key);
    buff.append("=");
    buff.append(value);
  }

  public String getQuery() throws IOException {
    if (sql != null) {
      return new String(Files.readAllBytes(FileSystems.getDefault().getPath(sql)));
    }
    return Joiner.on(" ").join(queryParts);
  }
}
