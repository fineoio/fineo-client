package io.fineo.client.tools.command;

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import io.fineo.client.FineoClientBuilder;
import io.fineo.client.model.schema.SchemaApi;
import io.fineo.client.model.schema.metric.DeleteMetricRequest;
import io.fineo.client.tools.option.MetricNameOption;
import io.fineo.client.tools.option.SchemaOption;

@Parameters(commandNames = "delete", commandDescription = "Delete a table (e.g. metric)")
public class DeleteMetric implements Command {

  @ParametersDelegate
  private MetricNameOption metric = new MetricNameOption();

  @Override
  public void run(FineoClientBuilder builder) throws Exception {
    try (SchemaApi.Metric metrics = builder.build(SchemaApi.Metric.class)) {
      DeleteMetricRequest request = new DeleteMetricRequest().setMetricName(metric.get());
      metrics.deleteMetric(request);
    }
  }
}
