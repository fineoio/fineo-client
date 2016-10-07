package io.fineo.client.tools.command;

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import io.fineo.client.FineoClientBuilder;
import io.fineo.client.model.schema.SchemaApi;
import io.fineo.client.model.schema.field.CreateFieldRequest;
import io.fineo.client.model.schema.metric.CreateMetricRequest;
import io.fineo.client.tools.option.SchemaOption;

@Parameters(commandNames = "create",
            commandDescription = "Create the specified metric and fields (Default command)")
public class CreateMetric implements Command {

  @ParametersDelegate
  private final SchemaOption schema;

  public CreateMetric(SchemaOption schema) {
    this.schema = schema;
  }

  @Override
  public void run(FineoClientBuilder builder) throws Exception {
    try (SchemaApi.Metric metrics = builder.build(SchemaApi.Metric.class);
         SchemaApi.Field fields = builder.build(SchemaApi.Field.class)) {
      CreateMetricRequest create = new CreateMetricRequest();
      // getting the class also sets the schema name
      create.setMetricName(schema.getName());
      metrics.createMetric(create);

      // create each field
      schema.getFields().forEach(field -> {
        CreateFieldRequest createField = new CreateFieldRequest();
        createField.setMetricName(schema.getName());
        createField.setFieldName(field.name);
        createField.setFieldType(field.type);

        fields.createField(createField);
      });
    }
  }
}
