package io.fineo.schema.timestamp;

import com.google.common.base.Preconditions;
import io.fineo.schema.Record;

import java.util.List;

/**
 * Parse the timestamp with various patterns. First the metric-level patterns are attempted,
 * followed by the global-level patterns. If both of those fail, we fall back to trying to parse the
 * field as a long.
 * <p>
 * Users can either specify names of various time formats, or build their own pattern according
 * to the DateTimeFormatter.
 * </p>
 */
public class MultiLevelTimestampParser implements TimestampParser {

  private final List<String> global;
  private final List<String> patterns;
  private final TimestampFieldExtractor extractor;

  public MultiLevelTimestampParser(List<String> metricPatterns, List<String> globalPatterns,
    TimestampFieldExtractor extractor) {
    this.global = globalPatterns;
    this.patterns = metricPatterns;
    this.extractor = extractor;
  }

  @Override
  public Long getTimestamp(Record record) {
    String key = extractor.getTimestampKey(record);
    String value = Preconditions.checkNotNull(record.getStringByField(key),
      "Could not find a timestamp in record: %s", record);
    // start with the metric-level aliases
    MultiPatternTimestampParser parser = new MultiPatternTimestampParser(patterns);
    Long parsed = parser.parse(value);

    // maybe the global level has something
    if (parsed == null) {
      parser.setPatterns(global);
      parsed = parser.parse(value);
    }

    // fall back to just trying to read a long
    if (parsed == null) {
      parsed = Long.valueOf(value);
    }
    return parsed;
  }
}
