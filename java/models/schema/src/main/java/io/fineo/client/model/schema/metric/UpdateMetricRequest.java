package io.fineo.client.model.schema.metric;

/**
 *
 */
public class UpdateMetricRequest extends MetricRequest{
  private String newDisplayName;
  private String aliases[];
  private String timestampPatterns[];

  public String getNewDisplayName() {
    return newDisplayName;
  }

  public void setNewDisplayName(String newDisplayName) {
    this.newDisplayName = newDisplayName;
  }

  public String[] getAliases() {
    return aliases;
  }

  public void setAliases(String[] aliases) {
    this.aliases = aliases;
  }

  public String[] getTimestampPatterns() {
    return timestampPatterns;
  }

  public void setTimestampPatterns(String[] timestampPatterns) {
    this.timestampPatterns = timestampPatterns;
  }
}
