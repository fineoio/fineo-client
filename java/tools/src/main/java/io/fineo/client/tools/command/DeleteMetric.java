package io.fineo.client.tools.command;

import com.beust.jcommander.Parameters;
import io.fineo.client.FineoClientBuilder;
import io.fineo.client.model.schema.SchemaApi;
import io.fineo.client.model.schema.metric.DeleteMetricRequest;
import io.fineo.client.tools.option.SchemaOption;

@Parameters(commandNames = "delete", commandDescription = "Delete a table (e.g. metric)")
public class DeleteMetric implements Command {
  private final SchemaOption schema;

  public DeleteMetric(SchemaOption schema) {
    this.schema = schema;
  }

  @Override
  public void run(FineoClientBuilder builder) throws Exception {
    try (SchemaApi.Metric metrics = builder.build(SchemaApi.Metric.class)) {
      DeleteMetricRequest request = new DeleteMetricRequest().setMetricName(schema.getName());
      metrics.deleteMetric(request);
    }
  }
}
