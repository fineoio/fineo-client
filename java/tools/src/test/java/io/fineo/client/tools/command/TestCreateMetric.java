package io.fineo.client.tools.command;

import io.fineo.client.FineoClientBuilder;
import io.fineo.client.model.schema.SchemaApi;
import io.fineo.client.model.schema.field.UpdateFieldRequest;
import io.fineo.client.tools.events.SchemaForTesting;
import io.fineo.client.tools.option.MetricClassOption;
import io.fineo.client.tools.option.MetricNameOption;
import io.fineo.client.tools.option.SchemaOption;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;

/**
 *
 */
public class TestCreateMetric {

  @Test
  public void testCreateWithAnnotatedType() throws Exception {
    SchemaOption option = new SchemaOption();
    MetricNameOption name = new MetricNameOption();
    name.set("metricname");
    option.metric = name;
    MetricClassOption clazz = new MetricClassOption();
    clazz.type = SchemaForTesting.class.getName();
    option.type = clazz;

    // setup mocks
    ClientBuilder builder = mockBuilder();

    CreateMetric create = new CreateMetric(option);
    create.run(builder.builder);

    UpdateFieldRequest update = new UpdateFieldRequest();
    update.setAliases(new String[]{"ts"});
    update.setFieldName("timestamp");
    update.setMetricName(name.get());
    Mockito.verify(builder.field).createField(any());
    builder.verify(update);
  }

  @Test
  public void testCreateWithCommandLineType() throws Exception {
    SchemaOption option = new SchemaOption();
    MetricNameOption name = new MetricNameOption();
    name.set("metricname");
    option.metric = name;
    option.fieldAndType.put("f1", "VARCHAR");

    // setup mocks
    ClientBuilder builder = mockBuilder();

    CreateMetric create = new CreateMetric(option);
    create.run(builder.builder);

    Mockito.verify(builder.field).createField(any());
  }

  private ClientBuilder mockBuilder(){
    return new ClientBuilder();
  }

  private class ClientBuilder{
    FineoClientBuilder builder = Mockito.mock(FineoClientBuilder.class);
    SchemaApi.Metric metric = Mockito.mock(SchemaApi.Metric.class);
    SchemaApi.Field field = Mockito.mock(SchemaApi.Field.class);
    SchemaApi.Management mgmt = Mockito.mock(SchemaApi.Management.class);

    public ClientBuilder() {
      Mockito.when(builder.build(eq(SchemaApi.Metric.class))).thenReturn(metric);
      Mockito.when(builder.build(eq(SchemaApi.Field.class))).thenReturn(field);
      Mockito.when(builder.build(eq(SchemaApi.Management.class))).thenReturn(mgmt);
    }

    private void verify(UpdateFieldRequest request) throws Exception {
      Mockito.verify(metric, times(1)).createMetric(any());
      Mockito.verify(field).close();
      Mockito.verify(mgmt, times(1)).updateCurrentSchemaManagement(any());
    }
  }
}
