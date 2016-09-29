package io.fineo.client.tools.command;

import com.beust.jcommander.Parameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fineo.client.FineoClientBuilder;
import io.fineo.client.model.schema.SchemaApi;
import io.fineo.client.model.schema.metric.ReadMetricResponse;
import io.fineo.client.tools.option.SchemaOption;

@Parameters(commandDescription = "Read the schema for the metric and output in JSON format")
public class ReadMetric implements Command {
  private final SchemaOption schema;

  public ReadMetric(SchemaOption schema) {
    this.schema = schema;
  }

  @Override
  public void run(FineoClientBuilder builder) throws Exception {
    try (SchemaApi.Metric metrics = builder.build(SchemaApi.Metric.class)) {
      ReadMetricResponse response = metrics.readMetric(schema.name);
      ObjectMapper mapper = new ObjectMapper();
      System.out.println(mapper.writeValueAsString(response));
    }
  }
}
