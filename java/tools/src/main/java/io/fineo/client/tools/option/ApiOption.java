package io.fineo.client.tools.option;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

public class ApiOption {
  @Parameter(names = "--url", description = "URL to call for api operations")
  public String url;

  @Parameter(names = "--api-key", description = "Api key to use")
  public String key;

  @ParametersDelegate
  public CredentialsOption credentials = new CredentialsOption();
}
