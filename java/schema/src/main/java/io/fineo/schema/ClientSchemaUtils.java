package io.fineo.schema;

import java.util.Collection;
import java.util.Optional;

/**
 *
 */
public class ClientSchemaUtils {

  private ClientSchemaUtils() {
  }


  public static Optional<String> getFieldInRecord(Record record, Collection<String> possibleNames) {
    return possibleNames == null ?
           Optional.empty() :
           possibleNames.stream()
                        .filter(name -> record.getField(name) != null)
                        .findAny();
  }
}
