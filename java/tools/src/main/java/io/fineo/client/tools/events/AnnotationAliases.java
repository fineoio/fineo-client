package io.fineo.client.tools.events;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class AnnotationAliases {

  private List<String> timestampPatterns = new ArrayList<>();
  private List<String> metricTypeAliases = new ArrayList<>();
  private List<String> timestampAliases = new ArrayList<>();

  public AnnotationAliases(Class schema) {
    for (Field f : schema.getDeclaredFields()) {
      for (Timestamp ts : f.getAnnotationsByType(Timestamp.class)) {
        if (ts.pattern() != null) {
          timestampPatterns.addAll(Arrays.asList(ts.pattern()));
        }
        timestampAliases.add(f.getName());
      }

      for (MetricType mt : f.getAnnotationsByType(MetricType.class)) {
        metricTypeAliases.add(f.getName());
      }
    }
  }

  public List<String> getTimestampPatterns() {
    return timestampPatterns;
  }

  public List<String> getMetricTypeAliases() {
    return metricTypeAliases;
  }

  public List<String> getTimestampAliases() {
    return timestampAliases;
  }
}
