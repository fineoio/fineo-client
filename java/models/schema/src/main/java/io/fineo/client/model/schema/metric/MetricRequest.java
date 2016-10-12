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

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof MetricRequest))
      return false;

    MetricRequest that = (MetricRequest) o;

    return getMetricName().equals(that.getMetricName());

  }

  @Override
  public int hashCode() {
    return getMetricName().hashCode();
  }
}
