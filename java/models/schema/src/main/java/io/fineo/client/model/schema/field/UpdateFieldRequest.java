package io.fineo.client.model.schema.field;

public class UpdateFieldRequest extends FieldRequest {
  private String[] aliases;

  public String[] getAliases() {
    return aliases;
  }

  public void setAliases(String[] aliases) {
    this.aliases = aliases;
  }
}
