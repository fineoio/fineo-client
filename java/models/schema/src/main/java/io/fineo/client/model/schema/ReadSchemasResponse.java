package io.fineo.client.model.schema;

import java.util.Map;

public class ReadSchemasResponse {
  private Map<String, String> idToMetricName;

  public Map<String, String> getIdToMetricName() {
    return idToMetricName;
  }

  public void setIdToMetricName(Map<String, String> idToMetricName) {
    this.idToMetricName = idToMetricName;
  }
}
