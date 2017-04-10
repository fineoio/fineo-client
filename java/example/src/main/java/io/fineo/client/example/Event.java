package io.fineo.client.example;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A simple example event to send. All events need:
 * <ul>
 *   <li>metrictype</li>
 *   <li>timestamp</li>
 * </ul>
 * Or a way to identify those fields through a metric key name or field alias.
 */
public class Event {
  private final String metrictype = ExampleDriver.METRIC_NAME;
  private String f1;
  private int f2;
  private long timestamp;

  public Event(String f1, int f2) {
    this.f1 = f1;
    this.f2 = f2;
    // every event needs a timestamp
    this.timestamp = System.currentTimeMillis();
  }

  @JsonProperty("metrictype")
  public String getMetrictype() {
    return metrictype;
  }

  @JsonProperty("f1")
  public String getF1() {
    return f1;
  }

  public Event withF1(String f1) {
    this.f1 = f1;
    return this;
  }

  @JsonProperty("f2")
  public int getF2() {
    return f2;
  }

  public Event withF2(int f2) {
    this.f2 = f2;
    return this;
  }

  @JsonProperty("timestamp")
  public long getTimestamp() {
    return timestamp;
  }

  public Event withTimestamp(long timestamp) {
    this.timestamp = timestamp;
    return this;
  }
}
