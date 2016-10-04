package io.fineo.schema;

import io.fineo.schema.timestamp.MultiPatternTimestampParser;
import org.junit.Test;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;

public class TestMultiPatternTimestampParser {

  @Test
  public void testNoPatterns() throws Exception {
    MultiPatternTimestampParser parser = new MultiPatternTimestampParser(null);
    assertEquals(null, parser.parse("12345"));
  }

  @Test
  public void testSimplePatternWithNoTimezone() throws Exception {
    MultiPatternTimestampParser parser =
      new MultiPatternTimestampParser(newArrayList("uuuu-MM-dd HH:mm:ss.SSS"));

    OffsetDateTime odt = OffsetDateTime.of(2015, 10, 12, 12, 34, 16, (int) TimeUnit.MILLISECONDS
      .toNanos(345), ZoneOffset.UTC);
    assertEquals(odt.toInstant().toEpochMilli(),
      parser.parse("2015-10-12 12:34:16.345").longValue());
  }

  @Test
  public void testExplictTimezoneSpecification() throws Exception {
    MultiPatternTimestampParser parser =
      new MultiPatternTimestampParser(newArrayList("uuuu-MM-dd HH:mm:ss.SSS@ UTC+1"));
    OffsetDateTime odt = OffsetDateTime.of(2015, 10, 12, 12, 34, 16, (int) TimeUnit.MILLISECONDS
      .toNanos(345), ZoneOffset.ofHours(1));
    assertEquals(odt.toInstant().toEpochMilli(),
      parser.parse("2015-10-12 12:34:16.345").longValue());
  }

  @Test
  public void testParseTimezone() throws Exception {
    MultiPatternTimestampParser parser =
      new MultiPatternTimestampParser(newArrayList("uuuu-MM-dd HH:mm:ss.SSS zzz"));
    ZonedDateTime zdt = ZonedDateTime.of(2015, 10, 12, 12, 34, 16, (int) TimeUnit.MILLISECONDS
      .toNanos(345), TimeZone.getTimeZone("PST").toZoneId());
    assertEquals(zdt.toInstant().toEpochMilli(),
      parser.parse("2015-10-12 12:34:16.345 PST").longValue());
  }
}
