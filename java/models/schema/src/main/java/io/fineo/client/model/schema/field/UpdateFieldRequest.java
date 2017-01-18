package io.fineo.client.model.schema.field;

import java.util.Arrays;

public class UpdateFieldRequest extends FieldRequest {
  private String newDisplayName;
  private String[] aliases;

  public String[] getAliases() {
    return aliases;
  }

  public void setAliases(String[] aliases) {
    this.aliases = aliases;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof UpdateFieldRequest))
      return false;

    if (!super.equals(o)) {
      return false;
    }

    UpdateFieldRequest that = (UpdateFieldRequest) o;
    return Arrays.equals(getAliases(), that.getAliases());
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(getAliases());
  }
}
