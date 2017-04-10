package io.fineo.client.example;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fineo.client.FineoClientBuilder;
import io.fineo.client.auth.CredentialsHelper;
import io.fineo.client.model.schema.SchemaApi;
import io.fineo.client.model.schema.field.CreateFieldRequest;
import io.fineo.client.model.schema.metric.CreateMetricRequest;
import io.fineo.client.model.write.StreamWrite;
import io.fineo.read.Driver;
import io.fineo.read.jdbc.FineoConnectionProperties;
import org.apache.commons.dbutils.handlers.MapListHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static java.lang.String.format;
import static java.lang.Thread.sleep;

/**
 * A simple example for creating a simple schema, uploading data and then reading that data back.
 * <p>
 * Arguments (in order):
 * <ol>
 * <li>API Key</li>
 * <li>Email (JDBC username)</li>
 * <li>Password (JDBC password)</li>
 * </ol>
 * <p>
 * After running this driver you should be able to see the 'example metric' schema in the
 * <a href="http://app.fineo.io>UI</a> and be able to read two rows from the 'example metric' table.
 *
 * @see DefaultAWSCredentialsProviderChain
 */
public class ExampleDriver {

  static final String METRIC_NAME = "example metric";

  public static void main(String[] args) throws Exception {
    String apikey = args[0];
    FineoClientBuilder builder = new FineoClientBuilder()
      .withApiKey(apikey)
      // you can also provide static credentials if you want to send as a 'device'
      .withCredentials(CredentialsHelper.getUserHelper(args[1], args[2]));
    setupSchema(builder);
    write(builder);
    read(apikey, args[1], args[2]);
  }

  private static void setupSchema(FineoClientBuilder builder) throws Exception {
    System.out.println("Starting to create table/metric type: " + METRIC_NAME);
    try (SchemaApi.Metric metrics = builder.build(SchemaApi.Metric.class);
         SchemaApi.Field fields = builder.build(SchemaApi.Field.class)) {
      CreateMetricRequest cm = new CreateMetricRequest().setMetricName(METRIC_NAME);
      metrics.createMetric(cm);

      // create fields matching the 'Event' to send below
      CreateFieldRequest field = new CreateFieldRequest()
        .setFieldType("VARCHAR")
        .setFieldName("f1")
        .setMetricName(METRIC_NAME);
      fields.createField(field);

      field.setFieldType("INTEGER")
           .setFieldName("f2");
      fields.createField(field);
    }
    System.out.println("Completed creating table/metric type: " + METRIC_NAME);
  }

  private static void write(FineoClientBuilder builder) throws Exception {
    System.out.println(format("Starting to write events to '%s'", METRIC_NAME));
    try (StreamWrite stream = builder.build(StreamWrite.class)) {
      // we can just send the regular event because we have:
      // - metrictype (case sensitive!)
      // - timestamp
      // - JSON annotations for fields such that the ObjectMapper can translate (a simple POJO
      // will work here as well)
      stream.writeEvent(new Event("first", 1));
      // wait to get a new timestamp, otherwise this will look like the same row
      sleep(1000);
      stream.writeEvent(new Event("second", 2));
    }
    System.out.println(format("Completed writing events to '%s'", METRIC_NAME));
  }

  private static void read(String apikey, String username, String password)
    throws InterruptedException, SQLException, JsonProcessingException {
    System.out.println("Starting to read data from Fineo");

    // ensure the driver is loaded
    Driver.load();

    // make the connection with our api key set.
    // you could also do this as:
    //   DriverManager.getConnection("jdbc:fineo:api_key=<key>", username, password)
    Properties props = new Properties();
    FineoConnectionProperties.API_KEY.set(props, apikey);
    props.setProperty("username", username);
    props.setProperty("passsword", password);

    // get the connection and attempt to read
    try (Connection conn = DriverManager.getConnection("jdbc:fineo", props)) {
      long start = System.currentTimeMillis();
      while (readInternal(conn) == 0) {
        sleep(100);
      }
      Duration d = Duration.ofMillis(System.currentTimeMillis() - start);
      System.out.println();
      System.out.println("Inconsistency window between write and read completion:");
      System.out.println(d.toMillis() + " ms (" + d.toMillis() / 1000.0 + " sec)");
    }
  }

  private static int readInternal(Connection conn)
    throws SQLException, JsonProcessingException {
    int rowCount = 0;
    try (Statement stmt = conn.createStatement();
         ResultSet set = stmt.executeQuery(format("SELECT * FROM `%s`", METRIC_NAME))) {
      // just page through the results and print them out
      List<Map<String, Object>> results;
      while (set.next()) {
        rowCount++;
        MapListHandler handler = new MapListHandler();
        results = handler.handle(set);
        ObjectMapper mapper = new ObjectMapper();
        String out = mapper.writeValueAsString(results);
        System.out.println(out);
      }
    }
    return rowCount;
  }
}
