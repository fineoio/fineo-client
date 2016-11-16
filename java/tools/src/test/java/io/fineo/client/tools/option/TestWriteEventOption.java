package io.fineo.client.tools.option;

import io.fineo.client.model.write.SingleStreamEventBase;
import io.fineo.client.tools.EventTypes;
import org.junit.Test;

import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class TestWriteEventOption {

  @Test
  public void testParseNoEventNumberSingleEvent() throws Exception {
    WriteEventOption option = optionForMetricFields("field.1");
    Object[] events = option.getEvents();
    assertEquals(1, events.length);
    validateMetric("1", events[0]);
  }

  @Test
  public void testParseSingleEventWithIndex() throws Exception {
    WriteEventOption option = optionForMetricFields("1.field.2");
    Object[] events = option.getEvents();
    assertEquals(1, events.length);
    validateMetric("2", events[0]);
  }

  @Test
  public void testMixNoEventNumberWithOtherEvents() throws Exception {
    WriteEventOption option = optionForMetricFields("field.1", "1.field.2");
    Object[] events = option.getEvents();
    assertEquals(2, events.length);
    validateMetric("1", events[0]);
    validateMetric("2", events[1]);
  }

  @Test
  public void testSettingTimestamp() throws Exception {
    WriteEventOption option = optionForMetricFields("field.1", "timestamp.2");
    Object[] events = option.getEvents();
    assertEquals(1, events.length);
    SingleStreamEventBase event = (SingleStreamEventBase) events[0];
    assertEquals(2,event.getTimestamp());
  }

  @Test
  public void testWriteEventCustomType() throws Exception {
    WriteEventOption opt = new WriteEventOption();
    opt.fields = newArrayList("field.1");
    Object[] events = opt.getEvents();
    assertEquals(1, events.length);
  }

  @Test
  public void testEventWithDotInFieldValue() throws Exception {
    WriteEventOption opt = new WriteEventOption();
    opt.fields = newArrayList("field.1.1");
    Object[] events = opt.getEvents();
    assertEquals(1, events.length);
    Map<String, String> event = (Map<String, String>) events[0];
    assertEquals("1.1", event.get("field"));
  }

  @Test
  public void testEventWithDotInFieldValueAndMultipleEvents() throws Exception {
    WriteEventOption opt = new WriteEventOption();
    opt.fields = newArrayList("0.field.1.1", "1.field.1.2");
    Object[] events = opt.getEvents();
    assertEquals(2, events.length);
    Map<String, String> event = (Map<String, String>) events[0];
    assertEquals("1.1", event.get("field"));
    event = (Map<String, String>) events[1];
    assertEquals("1.2", event.get("field"));
  }

  private void validateMetric(String fieldValue, Object streamEvent) {
    EventTypes.Metric event = (EventTypes.Metric) streamEvent;
    assertEquals(fieldValue, event.getField());
    assertTrue(event.getTimestamp() > 0);
    assertEquals("metric", event.getMetrictype());
  }

  private WriteEventOption optionForMetricFields(String... fields) {
    WriteEventOption option = new WriteEventOption();
    option.clazz = new MetricClassOption();
    option.clazz.type = "metric";
    option.fields = newArrayList(fields);
    return option;
  }
}
