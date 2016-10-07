package io.fineo.client.tools.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.fineo.client.FineoClientBuilder;
import io.fineo.client.model.schema.SchemaApi;
import io.fineo.client.model.schema.field.UpdateFieldRequest;
import io.fineo.client.model.schema.metric.UpdateMetricRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Parameters(commandNames = "update",
            commandDescription = "Update a metric")
public class UpdateMetric implements Command {

  @Parameter(names = "--metric-name", description = "Name or alias of the metric to update",
             required = true)
  private String metricName;

  @Parameter(names = "--alias",
             description = "Add an alias the metric.",
             variableArity = true)
  private List<String> metricAliases = new ArrayList<>();

  @Parameter(names = "--field-alias",
             description = "Add an alias to an existing field." +
                           "Syntax: --field-alias <field>=<alias>",
             variableArity = true)
  private List<String> fieldAliases = new ArrayList<>();

  @Parameter(names = "--timestamp-pattern",
             description = "Set the timestamp pattern(s) to use for the this metric",
             variableArity = true)
  private List<String> timestampPatterns = new ArrayList<>();

  private Multimap<String, String> fieldToAliases = ArrayListMultimap.create();

  @Override
  public void run(FineoClientBuilder builder) throws Exception {
    parse();

    try (SchemaApi.Metric metrics = builder.build(SchemaApi.Metric.class);
         SchemaApi.Field fields = builder.build(SchemaApi.Field.class)) {
      // update the metric, if any changes
      if (metricAliases.size() > 0 || timestampPatterns.size() > 0) {
        UpdateMetricRequest update = new UpdateMetricRequest();
        update.setMetricName(metricName);
        update.setAliases(metricAliases.toArray(new String[0]));
        update.setTimestampPatterns(timestampPatterns.toArray(new String[0]));
        metrics.updateMetric(update);
      }

      // update any field aliases that we find
      for (Map.Entry<String, Collection<String>> fas : fieldToAliases.asMap().entrySet()) {
        UpdateFieldRequest update = new UpdateFieldRequest();
        update.setMetricName(metricName);
        update.setFieldName(fas.getKey());
        update.setAliases(fas.getValue().toArray(new String[0]));
        fields.updateField(update);
      }
    }
  }

  private void parse() {
    for (String fa : fieldAliases) {
      String[] parts = fa.split("=");
      if (parts.length != 2) {
        throw new IllegalArgumentException("Found malformed field alias: " + fa);
      }
      fieldToAliases.put(parts[0], parts[1]);
    }
  }
}
