package io.fineo.client.tools.option;

import io.fineo.client.model.write.SingleStreamEventBase;
import io.fineo.client.tools.EventTypes;
import org.junit.Test;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class TestWriteEventOption {

  @Test
  public void testParseNoEventNumberSingleEvent() throws Exception {
    WriteEventOption option = optionForMetricFields("field.1");
    SingleStreamEventBase[] events = option.getEvents();
    assertEquals(1, events.length);
    validateMetric("1", events[0]);
  }

  @Test
  public void testParseSingleEventWithIndex() throws Exception {
    WriteEventOption option = optionForMetricFields("1.field.2");
    SingleStreamEventBase[] events = option.getEvents();
    assertEquals(1, events.length);
    validateMetric("2", events[0]);
  }

  @Test
  public void testMixNoEventNumberWithOtherEvents() throws Exception {
    WriteEventOption option = optionForMetricFields("field.1", "1.field.2");
    SingleStreamEventBase[] events = option.getEvents();
    assertEquals(2, events.length);
    validateMetric("1", events[0]);
    validateMetric("2", events[1]);
  }

  @Test
  public void testSettingTimestamp() throws Exception {
    WriteEventOption option = optionForMetricFields("field.1", "timestamp.2");
    SingleStreamEventBase[] events = option.getEvents();
    assertEquals(1, events.length);
    validateMetric("1", events[0]);
    assertEquals(2, events[0].getTimestamp());
  }

  private void validateMetric(String fieldValue, SingleStreamEventBase streamEvent) {
    EventTypes.Metric event = (EventTypes.Metric) streamEvent;
    assertEquals(fieldValue, event.getField());
    assertTrue(event.getTimestamp() > 0);
    assertEquals("metric", event.getMetrictype());
  }

  private WriteEventOption optionForMetricFields(String... fields) {
    WriteEventOption option = new WriteEventOption();
    SchemaOption schema = new SchemaOption();
    option.schema = schema;
    schema.type = "metric";
    option.fields = newArrayList(fields);
    return option;
  }
}
