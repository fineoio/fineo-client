package io.fineo.client.model.schema.field;

import java.util.Arrays;

public class ReadFieldResponse {
  public String name;
  public String [] aliases;
  public String type;

  public String getName() {
    return name;
  }

  public ReadFieldResponse setName(String name) {
    this.name = name;
    return this;
  }

  public String[] getAliases() {
    return aliases;
  }

  public ReadFieldResponse setAliases(String[] aliases) {
    this.aliases = aliases;
    return this;
  }

  public String getType() {
    return type;
  }

  public ReadFieldResponse setType(String type) {
    this.type = type;
    return this;
  }

  @Override
  public String toString() {
    return "ReadFieldResponse{" +
           "name='" + name + '\'' +
           ", aliases=" + Arrays.toString(aliases) +
           ", type='" + type + '\'' +
           '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof ReadFieldResponse))
      return false;

    ReadFieldResponse that = (ReadFieldResponse) o;

    if (!getName().equals(that.getName()))
      return false;
    // Probably incorrect - comparing Object[] arrays with Arrays.equals
    if (!Arrays.equals(getAliases(), that.getAliases()))
      return false;
    return getType().equals(that.getType());

  }

  @Override
  public int hashCode() {
    int result = getName().hashCode();
    result = 31 * result + Arrays.hashCode(getAliases());
    result = 31 * result + getType().hashCode();
    return result;
  }
}
