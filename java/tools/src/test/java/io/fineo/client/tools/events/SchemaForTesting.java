package io.fineo.client.tools.events;

public class SchemaForTesting {

  public static final String PATTERN = "uuuu-MM-dd HH:mm:ss.SSS@ UTC+1";

  @MetricType
  private String elem;
  @Timestamp(pattern = {PATTERN})
  private String ts;
  // simple field
  private String field1;

  public String getElem() {
    return elem;
  }

  public void setElem(String elem) {
    this.elem = elem;
  }

  public String getTs() {
    return ts;
  }

  public void setTs(String ts) {
    this.ts = ts;
  }

  public String getField1() {
    return field1;
  }

  public void setField1(String field1) {
    this.field1 = field1;
  }
}
