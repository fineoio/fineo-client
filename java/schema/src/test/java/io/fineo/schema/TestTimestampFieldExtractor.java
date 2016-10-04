package io.fineo.schema;

import io.fineo.schema.timestamp.TimestampFieldExtractor;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static io.fineo.schema.ClientSchemaProperties.TIMESTAMP_KEY;
import static org.junit.Assert.assertEquals;

public class TestTimestampFieldExtractor {

  @Test
  public void testFallbackToDefault() throws Exception {
    Map<String, Object> map = new HashMap<>();
    TimestampFieldExtractor extractor = new TimestampFieldExtractor(null);
    assertEquals(TIMESTAMP_KEY, extractor.getTimestampKey(new MapRecord(map)));

    extractor = new TimestampFieldExtractor(new ArrayList<>());
    assertEquals(TIMESTAMP_KEY, extractor.getTimestampKey(new MapRecord(map)));

    map.put(TIMESTAMP_KEY, "1234");
    assertEquals(TIMESTAMP_KEY, extractor.getTimestampKey(new MapRecord(map)));

    map.put("otherfield", 456);
    assertEquals(TIMESTAMP_KEY, extractor.getTimestampKey(new MapRecord(map)));

    extractor = new TimestampFieldExtractor(newArrayList("othertsfieldName"));
    assertEquals(TIMESTAMP_KEY, extractor.getTimestampKey(new MapRecord(map)));
  }

  @Test
  public void testSingleAlias() throws Exception {
    TimestampFieldExtractor extractor = new TimestampFieldExtractor(newArrayList("ts"));
    Map<String, Object> map = new HashMap<>();
    map.put("ts", 1234);
    assertEquals("ts", extractor.getTimestampKey(new MapRecord(map)));
  }

  @Test
  public void testFallbackToAliasesInOrder() throws Exception {
    Map<String, Object> map = new HashMap<>();
    map.put("f1", 1);
    map.put("f2", 1);
    map.put("f3", 1);
    TimestampFieldExtractor extractor = new TimestampFieldExtractor(newArrayList("f1", "f2", "f3"));
    MapRecord record = new MapRecord(map);
    assertEquals("f1", extractor.getTimestampKey(record));
    map.remove("f1");
    assertEquals("f2", extractor.getTimestampKey(record));
    map.remove("f2");
    assertEquals("f3", extractor.getTimestampKey(record));
    map.remove("f3");
    assertEquals(TIMESTAMP_KEY, extractor.getTimestampKey(record));
  }
}
