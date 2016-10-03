package io.fineo.schema.timestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.TemporalAccessor;
import java.util.List;

/**
 * Parse the timestamp from one of many patterns
 */
public class MultiPatternTimestampParser {

  public enum TimeFormats {
    // 2011-12-03T10:15:30 @ UTC
    ISO_DATE_TIME(DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.of(ZoneOffset.UTC.getId()))),
    // 2011-12-03T10:15:30+01:00
    ISO_OFFSET_DATE_TIME(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
    //2011-12-03T10:15:30+01:00[Europe/Paris]
    ISO_ZONED_DATE_TIME(DateTimeFormatter.ISO_ZONED_DATE_TIME),
    //2011-12-03T10:15:30Z
    ISO_INSTANT(DateTimeFormatter.ISO_INSTANT),
    // Tue, 3 Jun 2008 11:05:30 GMT
    RFC_1123_DATE_TIME(DateTimeFormatter.RFC_1123_DATE_TIME);

    private final DateTimeFormatter formatter;

    TimeFormats(DateTimeFormatter formatter) {
      this.formatter = formatter;
    }
  }

  private List<String> patterns;

  public MultiPatternTimestampParser(List<String> patterns) {
    this.patterns = patterns;
  }

  public void setPatterns(List<String> patterns) {
    this.patterns = patterns;
  }

  public Long parse(String value) {
    if (patterns == null) {
      return null;
    }
    for (String pattern : patterns) {
      if (pattern == null) {
        continue;
      }
      DateTimeFormatter formatter = getFormatter(pattern);
      try {
        TemporalAccessor time = formatter.parse(value);
        OffsetDateTime.parse(value, formatter);
        return Instant.from(time).toEpochMilli();
      } catch (DateTimeParseException e) {
        if (e.getMessage().contains("Unable to obtain OffsetDateTime")) {
          // try parsing just a local date time which we then zone to UTC
          OffsetDateTime odt = OffsetDateTime.of(LocalDateTime.parse(value, formatter),
            ZoneOffset.UTC);
          return odt.toInstant().toEpochMilli();
        }
        continue;
      }
    }
    return null;
  }

  public static DateTimeFormatter getFormatter(String format) {
    try {
      return MultiPatternTimestampParser.TimeFormats.valueOf(format.toUpperCase()).formatter;
    } catch (IllegalArgumentException e) {
      // not a predefined format, try something to convert it into a pattern
      return DateTimeFormatter.ofPattern(format).withResolverStyle(ResolverStyle.STRICT);
    }
  }
}
