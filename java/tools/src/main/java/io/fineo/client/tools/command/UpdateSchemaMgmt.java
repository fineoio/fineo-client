package io.fineo.client.tools.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fineo.client.FineoClientBuilder;
import io.fineo.client.model.schema.ReadSchemaManagement;
import io.fineo.client.model.schema.ReadSchemaManagementResponse;
import io.fineo.client.model.schema.SchemaApi;
import io.fineo.client.model.schema.SchemaManagementRequest;

import java.util.ArrayList;
import java.util.List;

@Parameters(commandNames = "update-mgmt",
            commandDescription = "Update the generic schema management.")
public class UpdateSchemaMgmt implements Command {

  @Parameter(names = "--metric-type-keys",
             description = "Set the keys that can be used to identify a metric type, rather than "
                           + "just 'metrictype: <type>'. REPEATABLE",
             variableArity = true)
  public List<String> metricTypeKeys = new ArrayList<>();

  @Parameter(names = "--timestamp-patterns",
             description = "Set base level set of timestamp patterns to use when parsing the "
                           + "timestamp field. REPEATABLE",
             variableArity = true)
  public List<String> timestampPatterns = new ArrayList<>();

  @Override
  public void run(FineoClientBuilder builder) throws Exception {
    try (SchemaApi.Management mgmt = builder.build(SchemaApi.Management.class)) {
      SchemaManagementRequest request = new SchemaManagementRequest();
      if (metricTypeKeys.size() > 0) {
        request.setMetricTypeKeys(metricTypeKeys.toArray(new String[0]));
      }
      if (timestampPatterns.size() > 0) {
        request.setTimestampPatterns(timestampPatterns.toArray(new String[0]));
      }
      mgmt.updateCurrentSchemaManagement(request);
    }
  }
}
