package io.fineo.client.tools;

import com.beust.jcommander.JCommander;
import io.fineo.client.FineoClientBuilder;
import io.fineo.client.model.schema.SchemaApi;
import io.fineo.client.model.schema.field.CreateFieldRequest;
import io.fineo.client.model.schema.metric.CreateMetricRequest;
import io.fineo.client.tools.option.ApiOption;
import io.fineo.client.tools.option.HelpOption;
import io.fineo.client.tools.option.SchemaOption;

import static io.fineo.client.tools.option.HelpOption.help;

/**
 * Simple tool to create schema changes.
 * <p>
 * Supports:
 * <ol>
 * <li>Creating a metric type based on a java class</li>
 * </ol>
 */
public class Schema {

  public static void main(String[] args) throws Exception {
    ApiOption api = new ApiOption();
    SchemaOption schema = new SchemaOption();
    HelpOption help = help();
    JCommander commander = new JCommander(new Object[]{api, schema, help});
    commander.parse(args);
    help.check(commander);

    // ensure schema is loaded
    schema.load();

    // get ready to create clients
    FineoClientBuilder builder = new FineoClientBuilder()
      .withApiKey(api.key)
      .withEndpoint(api.url)
      .withCredentials(api.credentials.get());

    try (SchemaApi.Metric metrics = builder.build(SchemaApi.Metric.class);
         SchemaApi.Field fields = builder.build(SchemaApi.Field.class)) {
      CreateMetricRequest create = new CreateMetricRequest();
      // getting the class also sets the schema name
      create.setMetricName(schema.name);
      metrics.createMetric(create);

      // create each field
      schema.getFields().forEach(field -> {
        CreateFieldRequest createField = new CreateFieldRequest();
        createField.setMetricName(schema.name);
        createField.setFieldName(field.name);
        createField.setFieldType(field.type);

        fields.createField(createField);
      });
    }
  }
}
