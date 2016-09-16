package io.fineo.client.model.schema;

public class SchemaManagementRequest {

  private String[] metricTypeKeys;
  private String[] timestampPatterns;

  public String[] getMetricTypeKeys() {
    return metricTypeKeys;
  }

  public void setMetricTypeKeys(String[] metricTypeKeys) {
    this.metricTypeKeys = metricTypeKeys;
  }

  public String[] getTimestampPatterns() {
    return timestampPatterns;
  }

  public void setTimestampPatterns(String[] timestampPatterns) {
    this.timestampPatterns = timestampPatterns;
  }
}
