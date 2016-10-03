package io.fineo.client.tools;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import io.fineo.client.tools.option.HelpOption;
import io.fineo.schema.ClientSchemaProperties;
import io.fineo.schema.MapRecord;
import io.fineo.schema.timestamp.MultiLevelTimestampParser;
import io.fineo.schema.timestamp.TimestampFieldExtractor;

import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static io.fineo.client.tools.option.HelpOption.help;

/**
 * Check that your timestamp parsing format works as expected.
 */
public class TimestampValidation {

  public static void main(String[] args) {
    HelpOption help = help();
    TimestampValidation validation = new TimestampValidation();
    JCommander commander = new JCommander(new Object[]{help, validation});
    commander.parse(args);
    help.check(commander);

    validation.run();
  }

  @Parameter(names = {"-p", "--pattern"}, description = "Pattern or pattern name to check")
  private String patternOrName;

  @Parameter(names = {"-v", "--value"}, description = "Value to parse with the specified pattern")
  private String value;

  private void run() {
    Map<String, Object> record = new HashMap<>();
    record.put(ClientSchemaProperties.TIMESTAMP_KEY, value);

    MultiLevelTimestampParser parser = new MultiLevelTimestampParser(newArrayList(patternOrName),
      null, new TimestampFieldExtractor(null));
    long timestamp = parser.getTimestamp(new MapRecord(record));
    System.out.println("Timestamp parsed to Unix epoch: " + timestamp);
    Instant instant = Instant.ofEpochMilli(timestamp);
    System.out.println("Which is ");
    System.out.println(" ISO_INSTANT: " + instant);
    System.out.println(" Local:       " + instant.atZone(ZoneId.systemDefault()));
  }
}
