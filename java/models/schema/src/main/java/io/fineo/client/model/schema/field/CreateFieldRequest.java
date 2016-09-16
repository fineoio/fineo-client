package io.fineo.client.model.schema.field;


public class CreateFieldRequest extends FieldRequest {

  private String fieldType;
  private String[] aliases;

  public String[] getAliases() {
    return aliases;
  }

  public CreateFieldRequest setAliases(String[] aliases) {
    this.aliases = aliases;
    return this;
  }

  public String getFieldType() {
    return fieldType;
  }

  public CreateFieldRequest setFieldType(String fieldType) {
    this.fieldType = fieldType;
    return this;
  }
}
