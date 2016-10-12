package io.fineo.client.tools;

import com.beust.jcommander.JCommander;
import io.fineo.client.FineoClientBuilder;
import io.fineo.client.tools.command.Command;
import io.fineo.client.tools.command.CreateMetric;
import io.fineo.client.tools.command.DeleteMetric;
import io.fineo.client.tools.command.ReadMetric;
import io.fineo.client.tools.command.ReadSchemaMgmt;
import io.fineo.client.tools.command.UpdateMetric;
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
    HelpOption help = help();
    ApiOption api = new ApiOption();

    SchemaOption schema = new SchemaOption();
    CreateMetric create = new CreateMetric(schema);
    ReadMetric read = new ReadMetric(schema);
    UpdateMetric update = new UpdateMetric();
    DeleteMetric delete = new DeleteMetric(schema);
    ReadSchemaMgmt readSchema = new ReadSchemaMgmt();

    JCommander commander = new JCommander(new Object[]{api, help});
    commander.addCommand(create);
    commander.addCommand(read);
    commander.addCommand(update);
    commander.addCommand(delete);
    commander.addCommand(readSchema);

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
