package io.fineo.client.model.schema.field;

import java.util.Arrays;

public class UpdateFieldRequest extends FieldRequest {
  private String newDisplayName;
  private String[] aliases;

  public String[] getAliases() {
    return aliases;
  }

  public UpdateFieldRequest setAliases(String[] aliases) {
    this.aliases = aliases;
    return this;
  }

  public String getNewDisplayName() {
    return newDisplayName;
  }

  public UpdateFieldRequest setNewDisplayName(String newDisplayName) {
    this.newDisplayName = newDisplayName;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof UpdateFieldRequest))
      return false;
    if (!super.equals(o))
      return false;

    UpdateFieldRequest that = (UpdateFieldRequest) o;

    if (getNewDisplayName() != null ? !getNewDisplayName().equals(that.getNewDisplayName()) :
        that.getNewDisplayName() != null)
      return false;
    // Probably incorrect - comparing Object[] arrays with Arrays.equals
    return Arrays.equals(getAliases(), that.getAliases());

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (getNewDisplayName() != null ? getNewDisplayName().hashCode() : 0);
    result = 31 * result + Arrays.hashCode(getAliases());
    return result;
  }
}
