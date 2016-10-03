package io.fineo.schema;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

/**
 * Simple {@link Record} that is directly backed by a {@link Map}
 */
public class MapRecord implements Record {
  private final Map<String, Object> map;

  public MapRecord(Map<String, Object> fields) {
    this.map = fields;
  }

  @Override
  public Boolean getBooleanByField(String fieldName) {
    return translate(fieldName, Boolean::valueOf, Boolean.class);
  }

  @Override
  public Integer getIntegerByField(String fieldName) {
    return translate(fieldName, Integer::valueOf, Integer.class);
  }

  @Override
  public Long getLongByFieldName(String fieldName) {
    return translate(fieldName, Long::valueOf, Long.class);
  }

  @Override
  public Float getFloatByFieldName(String fieldName) {
    return translate(fieldName, Float::valueOf, Float.class);
  }

  @Override
  public Double getDoubleByFieldName(String fieldName) {
    return translate(fieldName, Double::valueOf, Double.class);
  }

  @Override
  public ByteBuffer getBytesByFieldName(String fieldName) {
    return (ByteBuffer) map.get(fieldName);
  }

  @Override
  public String getStringByField(String fieldName) {
    return translate(fieldName, s -> s.toString(), String.class);
  }

  @Override
  public Collection<String> getFieldNames() {
    return map.keySet();
  }

  @Override
  public Iterable<Map.Entry<String, Object>> getFields() {
    return map.entrySet();
  }

  @Override
  public Object getField(String name) {
    return map.get(name);
  }

  @Override
  public String toString() {
    return this.map.toString();
  }

  private <T> T translate(String fieldName, Function<String, T> func, Class<T> clazz) {
    Object o = map.get(fieldName);
    if(o == null){
      return null;
    }
    if (o.getClass().isAssignableFrom(clazz)) {
      return (T) o;
    } else if (!(o instanceof String)) {
      o = o.toString();
    }
    return func.apply((String) o);
  }
}
