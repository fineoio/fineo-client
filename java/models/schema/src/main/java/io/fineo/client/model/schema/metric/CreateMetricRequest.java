package io.fineo.client.model.schema.metric;

public class CreateMetricRequest extends MetricRequest{

  private String[] aliases;

  public String[] getAliases() {
    return aliases;
  }

  public CreateMetricRequest setAliases(String[] aliases) {
    this.aliases = aliases;
    return this;
  }
}
