package io.fineo.client.model.schema.field;

import io.fineo.client.model.schema.metric.MetricRequest;

public class FieldRequest extends MetricRequest {

  private String fieldName;

  public String getFieldName() {
    return fieldName;
  }

  public <T extends FieldRequest> T setFieldName(String fieldName) {
    this.fieldName = fieldName;
    return (T) this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof FieldRequest))
      return false;

    if(!super.equals(o)){
      return false;
    }

    FieldRequest that = (FieldRequest) o;

    return getFieldName().equals(that.getFieldName());
  }

  @Override
  public int hashCode() {
    return getFieldName().hashCode();
  }
}
