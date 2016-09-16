package io.fineo.client.model.schema.metric;

public class MetricRequest {

  private String metricName;

  public String getMetricName() {
    return metricName;
  }

  public <T extends MetricRequest> T setMetricName(String metricName) {
    this.metricName = metricName;
    return (T) this;
  }
}
