package io.fineo.client.tools.option;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import io.fineo.client.model.write.SingleStreamEventBase;
import io.fineo.client.tools.EventTypes;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Options around creating/managing schemas
 */
public class SchemaOption {

  @Parameter(names = "--type", description = "Generic type name of a known EventType or class name "
                                             + "of a model for the metric schema")
  String type;

  @Parameter(names = "--metric-name", description = "Name of the metric. If none specified, uses a "
                                                    + "form of the type class name")
  private String name;

  @DynamicParameter(names = "-F",
                    description = "Field name and type specification. E.g. -Ffield1=VARCHAR")
  private Map<String, String> fieldAndType = new HashMap<>();

  public String getName() {
    if (name == null) {
      return this.type;
    }
    return this.name;
  }

  public List<FieldInstance> getFields() throws ClassNotFoundException {
    if (type != null) {
      return loadFromClass();
    }

    // just convert the specified fields
    return fieldAndType.entrySet().stream().map(entry -> new FieldInstance(entry.getKey(), entry
      .getValue())).collect(Collectors.toList());
  }

  private List<FieldInstance> loadFromClass() throws ClassNotFoundException {
    Class clazz = getClazz();

    // get all the fields as simple get/set methods. this is rather crude, but its just a simple
    // tool with simple goals. If you want to get fancier, its recommended to write more java
    // code around a SchemaApi instance that handles the loading of data for you
    List<FieldInstance> fields = new ArrayList<>();
    for (Method method : clazz.getDeclaredMethods()) {
      String name = method.getName();
      if (name.startsWith("get")) {
        String suffix = name.substring(3);
        String fieldName = suffix.substring(0, 1).toLowerCase() + suffix.substring(1);
        Class type = method.getReturnType();
        String typeName;
        switch (type.getTypeName()) {
          case "java.lang.String":
            typeName = "STRING";
            break;
          case "int":
            typeName = "INTEGER";
            break;
          case "long":
            typeName = "LONG";
            break;
          case "double":
            typeName = "DOUBLE";
            break;
          case "float":
            typeName = "FLOAT";
            break;
          case "boolean":
            typeName = "BOOLEAN";
            break;
          case "byte[]":
            typeName = "BINARY";
            break;
          default:
            throw new IllegalArgumentException(
              "Schema tool does not support type: " + type.getTypeName());
        }
        fields.add(new FieldInstance(fieldName, typeName));
      }
    }
    return fields;
  }

  public Class<? extends SingleStreamEventBase> getClazz() throws ClassNotFoundException {
    Class clazz = EventTypes.EVENTS.get(type);
    if (clazz == null) {
      clazz = Class.forName(type).asSubclass(SingleStreamEventBase.class);
    }
    if (this.name == null) {
      this.name = type;
    }
    return clazz;
  }

  public static class FieldInstance {
    public String name;
    public String type;

    public FieldInstance(String name, String type) {
      this.name = name;
      this.type = type;
    }

    @Override
    public String toString() {
      return "FieldInstance{" +
             "name='" + name + '\'' +
             ", type='" + type + '\'' +
             '}';
    }
  }
}
