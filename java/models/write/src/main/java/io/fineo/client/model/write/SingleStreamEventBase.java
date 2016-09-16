package io.fineo.client.model.write;

/**
 * Base class for a stream event. Actually stream events should have at least one 'metrictype'
 * field, though the field may not necessarily be named as such, if you have a custom metric key
 * specified for the overall schema.
 */
public abstract class SingleStreamEventBase {

  private long timestamp;

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }
}
