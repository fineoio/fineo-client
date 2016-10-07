package io.fineo.client.tools.option;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import io.fineo.client.model.write.SingleStreamEventBase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A bit more complicated, but basically we create a set of write events from a schema model. We
 * only support writing one event at a time.
 */
public class WriteEventOption {

  private ObjectMapper mapper = new ObjectMapper();

  // so we can get the type of the event
  @ParametersDelegate
  SchemaOption schema = new SchemaOption();

  @Parameter(names = "--field", variableArity = true,
             description = "[event number.]<field name>.<value> The event number is only "
                           + "necessary if more than one event is being written at a time")
  List<String> fields = new ArrayList<>();

  public SingleStreamEventBase[] getEvents() throws ClassNotFoundException {
    Preconditions.checkArgument(fields.size() != 0, "No events/fields specified to send!");
    Class<? extends SingleStreamEventBase> clazz = schema.getClazz();

    Map<Integer, Map<String, Object>> events = new HashMap<>();
    for (String field : fields) {
      String[] parts = field.split("[.]");
      Integer index;
      // its the only field to send, so its index is 0
      if (parts.length == 2) {
        index = 0;
      } else {
        index = Integer.valueOf(parts[0]);
        parts = new String[]{parts[1], parts[2]};
      }
      Map<String, Object> event = events.get(index);
      if (event == null) {
        event = new HashMap<>();
        events.put(index, event);
      }

      event.put(parts[0], parts[1]);
    }


    long ts = System.currentTimeMillis();
    for (Map<String, Object> event : events.values()) {
      // all events need a metric type too
      event.put("metrictype", schema.getName());

      // ensure all the events have a timestamp field
      if (!event.containsKey("timestamp")) {
        event.put("timestamp", ts);
      }
    }

    List<SingleStreamEventBase> eventList =
      events.entrySet().stream()
            .sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
            .map(e -> e.getValue())
            .map(event -> {
              String msg = null;
              try {
                msg = mapper.writeValueAsString(event);
                return mapper.readValue(msg, clazz);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            })
            .collect(Collectors.toList());
    return eventList.toArray(new SingleStreamEventBase[0]);
  }
}
