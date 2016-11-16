package io.fineo.client.tools.option;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import io.fineo.client.model.write.SingleStreamEventBase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;

/**
 * A bit more complicated, but basically we create a set of write events from a schema model. We
 * only support writing one event at a time.
 */
public class WriteEventOption {

  private ObjectMapper mapper = new ObjectMapper();

  // so we can get the type of the event
  @ParametersDelegate
  MetricClassOption clazz = new MetricClassOption();

  @ParametersDelegate
  MetricNameOption name = new MetricNameOption();

  @Parameter(names = "--field", variableArity = true,
             description = "[event number.]<field name>.<value> The event number is only "
                           + "necessary if more than one event is being written at a time")
  List<String> fields = new ArrayList<>();

  public Object[] getEvents() throws ClassNotFoundException {
    Preconditions.checkArgument(fields.size() != 0, "No events/fields specified to send!");
    Class<? extends SingleStreamEventBase> clazz = this.clazz.getClazz();

    Map<Integer, Map<String, Object>> events = new HashMap<>();
    for (String field : fields) {
      List<String> parts = newArrayList(field.split("[.]"));
      Integer index;
      // its the only field to send, so its index is 0
      if (parts.size() == 2) {
        index = 0;
      } else {
        try {
          index = Integer.valueOf(parts.get(0));
          parts.remove(0);
        } catch (NumberFormatException e) {
          // oops, its not a number. Instead, its a field name, with a field that has a '.' in
          // the value
          index = 0;
        }
      }
      Map<String, Object> event = events.get(index);
      if (event == null) {
        event = new HashMap<>();
        events.put(index, event);
      }

      event.put(parts.get(0), Joiner.on(".").join(parts.subList(1, parts.size())));
    }


    long ts = System.currentTimeMillis();
    for (Map<String, Object> event : events.values()) {
      // try to specify a type for the event
      String type = SchemaOption.getMetricName(this.name, this.clazz);
      event.put("metrictype", type);

      // ensure all the events have a timestamp field
      if (!event.containsKey("timestamp")) {
        event.put("timestamp", ts);
      }
    }

    List<Object> eventList =
      events.entrySet().stream()
            .sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
            // just get the event
            .map(e -> e.getValue())
            // attempt "casting" the event to the specified type (if one was specified)
            .map(event -> {
              if (clazz == null) {
                return event;
              }
              String msg = null;
              try {
                msg = mapper.writeValueAsString(event);
                return mapper.readValue(msg, clazz);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            })
            .collect(Collectors.toList());
    return eventList.toArray();
  }
}
