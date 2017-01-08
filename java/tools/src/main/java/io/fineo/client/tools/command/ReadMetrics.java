package io.fineo.client.tools.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fineo.client.FineoClientBuilder;
import io.fineo.client.model.schema.ReadSchemaManagement;
import io.fineo.client.model.schema.ReadSchemaManagementResponse;
import io.fineo.client.model.schema.ReadSchemasResponse;
import io.fineo.client.model.schema.SchemaApi;

@Parameters(commandNames = "metrics",
            commandDescription = "Read the names & ids of the metrics")
public class ReadMetrics implements Command {

  @Parameter(names = {"--pp", "--pretty-print"})
  public boolean prettyPrint = false;

  @Override
  public void run(FineoClientBuilder builder) throws Exception {
    try (SchemaApi.Management mgmt = builder.build(SchemaApi.Management.class)) {
      ReadSchemasResponse response = mgmt.getSchemas();
      String msg;
      if (prettyPrint) {
        msg = new ObjectMapper().writerWithDefaultPrettyPrinter()
                                .writeValueAsString(response);
      } else {
        msg = new ObjectMapper().writeValueAsString(response);
      }
      System.out.println(msg);
    }
  }
}
