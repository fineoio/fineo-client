package io.fineo.client.tools;

import io.fineo.client.model.write.SingleStreamEventBase;

import java.util.HashMap;
import java.util.Map;

/**
 * static mapping of event types to a concrete class
 */
public class EventTypes {

  public static Map<String, Class<? extends SingleStreamEventBase>> EVENTS = new HashMap<>();

  static {
    EVENTS.put("metric", Metric.class);
  }

  public static class EventBase extends SingleStreamEventBase {
    private String metrictype;

    public String getMetrictype() {
      return metrictype;
    }

    public void setMetrictype(String metrictype) {
      this.metrictype = metrictype;
    }
  }

  public static class Metric extends EventBase {
    private String field;

    public String getField() {
      return field;
    }

    public void setField(String field) {
      this.field = field;
    }

    @Override
    public String toString() {
      return "Metric{" +
             "timestamp='" + getTimestamp() + "'," +
             "metrictype='" + getMetrictype() + "'," +
             "field='" + field + '\'' +
             '}';
    }
  }
}
