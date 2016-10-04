package io.fineo.schema.timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse the timestamp from one of many patterns
 */
public class MultiPatternTimestampParser {

  private static final Logger LOG = LoggerFactory.getLogger(MultiPatternTimestampParser.class);
  private static final String EXPLICIT_ZONE_PATTERN = ".*[@](?<zone>.*)";
  private static final Pattern ZONE_PATTERN = Pattern.compile(EXPLICIT_ZONE_PATTERN);

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

      TimestampFormatter formatter = getFormatter(pattern);
      try {
        return ZonedDateTime.parse(value, formatter.formatter).toInstant().toEpochMilli();
      } catch (DateTimeParseException e) {
        LOG.debug(e.getMessage());
        if (e.getMessage().contains("Unable to obtain")) {
          // try parsing just a local date time which we then zone to UTC
          ZonedDateTime odt = ZonedDateTime.of(LocalDateTime.parse(value, formatter.formatter),
            formatter.explicitZone);
          return odt.toInstant().toEpochMilli();
        }
        continue;
      }
    }
    return null;
  }

  public static class TimestampFormatter {
    private DateTimeFormatter formatter;
    private ZoneId explicitZone;

    public TimestampFormatter(DateTimeFormatter formatter, ZoneId explicitZone) {
      this.formatter = formatter;
      this.explicitZone = explicitZone;
    }
  }

  public static TimestampFormatter getFormatter(String format) {
    Matcher matcher = ZONE_PATTERN.matcher(format);
    ZoneId id = ZoneId.of("UTC");
    if (matcher.matches()) {
      String zone = matcher.group("zone");
      id = ZoneId.of(zone.trim());
      // strip off the ending
      format = format.substring(0, format.indexOf("@"));
      LOG.debug("Using format: '{}' with fixed zone: '{}'", format, id);
    }
    DateTimeFormatter formatter = parser(format);
    return new TimestampFormatter(formatter, id);
  }

  private static DateTimeFormatter parser(String format) {
    try {
      return MultiPatternTimestampParser.TimeFormats.valueOf(format.toUpperCase()).formatter;
    } catch (IllegalArgumentException e) {
      // not a predefined format, try something to convert it into a pattern
      return DateTimeFormatter.ofPattern(format).withResolverStyle(ResolverStyle.STRICT);
    }
  }
}
