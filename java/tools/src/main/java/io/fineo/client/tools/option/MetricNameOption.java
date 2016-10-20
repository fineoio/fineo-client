package io.fineo.client.tools.option;

import com.beust.jcommander.Parameter;
import com.google.common.annotations.VisibleForTesting;

public class MetricNameOption {

  @Parameter(names = "--metric-name", description = "Name of the metric. If none specified, uses a "
                                                    + "form of the type class name")
  private String name;

  public String get() {
    return name;
  }

  @VisibleForTesting
  public void set(String metricname) {
    this.name = metricname;
  }
}
