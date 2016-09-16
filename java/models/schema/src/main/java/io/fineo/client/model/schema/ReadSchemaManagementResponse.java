package io.fineo.client.model.schema;


public class ReadSchemaManagementResponse {
  private String[] timestampPatterns;
  private String [] metricKeys;

  public String[] getTimestampPatterns() {
    return timestampPatterns;
  }

  public void setTimestampPatterns(String[] timestampPatterns) {
    this.timestampPatterns = timestampPatterns;
  }

  public String[] getMetricKeys() {
    return metricKeys;
  }

  public void setMetricKeys(String[] metricKeys) {
    this.metricKeys = metricKeys;
  }
}
