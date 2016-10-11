package io.fineo.client.tools.events;

public class SchemaForTesting {

  public static final String PATTERN = "uuuu-MM-dd HH:mm:ss.SSS@ UTC+1";

  @MetricType
  private String elem;
  @Timestamp(pattern = {PATTERN})
  private String ts;
  // simple field
  private String field1;
}
