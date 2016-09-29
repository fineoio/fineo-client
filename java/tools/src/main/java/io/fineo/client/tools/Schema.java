package io.fineo.client.tools;

import com.beust.jcommander.JCommander;
import io.fineo.client.FineoClientBuilder;
import io.fineo.client.tools.command.Command;
import io.fineo.client.tools.command.CreateMetric;
import io.fineo.client.tools.command.ReadMetric;
import io.fineo.client.tools.option.ApiOption;
import io.fineo.client.tools.option.HelpOption;
import io.fineo.client.tools.option.SchemaOption;

import static io.fineo.client.tools.option.HelpOption.help;

/**
 * Simple tool to create schema changes.
 * <p>
 * Supports:
 * <ol>
 * <li>create: Creating a table (metric) based on a java class</li>
 * <li>read: Creating a table (metric) based on a java class</li>
 * </ol>
 */
public class Schema {

  public static void main(String[] args) throws Exception {
    ApiOption api = new ApiOption();
    SchemaOption schema = new SchemaOption();
    CreateMetric create = new CreateMetric(schema);
    ReadMetric read = new ReadMetric(schema);
    HelpOption help = help();
    JCommander commander = new JCommander(new Object[]{api, schema, help});
    commander.addCommand("create", create);
    commander.addCommand("read", read);

    commander.parse(args);
    help.check(commander);

    //prepare the client
    FineoClientBuilder builder = new FineoClientBuilder()
      .withApiKey(api.key)
      .withEndpoint(api.url)
      .withCredentials(api.credentials.get());

    Command command;
    String parsed = commander.getParsedCommand();
    if (parsed == null) {
      command = create;
    } else {
      JCommander c = commander.getCommands().get(commander.getParsedCommand());
      if (c == null) {
        System.err.println("Unrecognized command!");
        commander.usage();
        System.exit(1);
      }
      command = (Command) c.getObjects().get(0);
    }

    // run the command
    command.run(builder);
  }
}
