package io.fineo.schema.timestamp;

import io.fineo.schema.ClientSchemaProperties;
import io.fineo.schema.ClientSchemaUtils;
import io.fineo.schema.Record;

import java.util.List;

/**
 * Extract the timestamp field based on the aliases for the field
 */
public class TimestampFieldExtractor {

  private final List<String> aliases;

  public TimestampFieldExtractor(List<String> timestampAliases) {
    this.aliases = timestampAliases;
  }

  public String getTimestampKey(Record record) {
    return ClientSchemaUtils.getFieldInRecord(record, aliases)
                            .orElse(ClientSchemaProperties.TIMESTAMP_KEY);
  }
}
