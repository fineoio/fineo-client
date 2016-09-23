package io.fineo.client.tools;

import com.beust.jcommander.JCommander;
import com.google.common.base.Joiner;
import io.fineo.client.FineoClientBuilder;
import io.fineo.client.model.write.SingleStreamEventBase;
import io.fineo.client.model.write.StreamRecordsResponse;
import io.fineo.client.model.write.StreamWrite;
import io.fineo.client.tools.option.ApiOption;
import io.fineo.client.tools.option.HelpOption;
import io.fineo.client.tools.option.SequentialEventsOption;
import io.fineo.client.tools.option.WriteEventOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static io.fineo.client.tools.option.HelpOption.help;

/**
 * Simple tool to write events based on a java class model
 */
public class Stream {

  private static final Logger LOG = LoggerFactory.getLogger(Stream.class);

  public static void main(String[] args) throws Exception {
    ApiOption api = new ApiOption();
    WriteEventOption events = new WriteEventOption();
    SequentialEventsOption write = new SequentialEventsOption();
    HelpOption help = help();
    JCommander commander = new JCommander(new Object[]{api, events, write, help});
    commander.parse(args);
    help.check(commander);

    FineoClientBuilder builder = new FineoClientBuilder()
      .withApiKey(api.key)
      .withEndpoint(api.url)
      .withCredentials(api.credentials.get());

    try (StreamWrite stream = builder.build(StreamWrite.class)) {
      SingleStreamEventBase[] event = events.getEvents();
      if (write.sequential) {
        for (SingleStreamEventBase e : event) {
          stream.writeEvent(e);
        }
      } else {
        StreamRecordsResponse response = stream.write(event);
        String eventString = Arrays.toString(event);
        assert response.getFailedRecordCount() == 0 :
          "Got some failed records when writing to stream! " +
          "\n events: \n" + eventString
          + "\n results: \n" + Arrays.toString(response.getRecords());

      }
      LOG.info("Wrote events: \n{}", Joiner.on("\n").join(event));
    }
  }
}
