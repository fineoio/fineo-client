package io.fineo.client.tools.option;

import com.beust.jcommander.Parameter;
import com.google.common.annotations.VisibleForTesting;
import io.fineo.client.tools.EventTypes;

public class MetricClassOption {

  @Parameter(names = "--type", description = "Generic type name of a known EventType or class name "
                                             + "of a model for the metric schema")
  public String type;

  public String getTypeName() {
    return this.type;
  }

  public Class getClazz() throws ClassNotFoundException {
    if (type == null) {
      return null;
    }

    Class clazz = EventTypes.EVENTS.get(type);
    if (clazz == null) {
      clazz = Class.forName(type);
    }

    return clazz;
  }

  @VisibleForTesting
  public MetricClassOption setType(String type) {
    this.type = type;
    return this;
  }
}
