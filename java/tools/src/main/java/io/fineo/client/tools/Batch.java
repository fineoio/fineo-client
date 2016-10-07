package io.fineo.client.tools;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fineo.client.FineoClientBuilder;
import io.fineo.client.model.write.BatchUploadRemoteS3File;
import io.fineo.client.model.write.BatchWrite;
import io.fineo.client.model.write.SingleStreamEventBase;
import io.fineo.client.tools.option.ApiOption;
import io.fineo.client.tools.option.BatchUploadOption;
import io.fineo.client.tools.option.HelpOption;
import io.fineo.client.tools.option.SchemaOption;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static io.fineo.client.tools.option.HelpOption.help;

/**
 * Simple tool batch upload events.
 */
@Parameters(commandDescription =
              "Upload batch events.\n Supports either:" +
              "\n\tuploading an existing s3 file (file name must start with s3)" +
              "\n\tUploading a local .json or .csv file (ending determines file type) as a group "
              + "of files. The remote file (for checking) is given a random name printed to the "
              + "console" +
              "\n\tChecking the status of a previously uploaded file.")
public class Batch {

  @ParametersDelegate
  private ApiOption api = new ApiOption();

  @ParametersDelegate
  private BatchUploadOption batchOpt = new BatchUploadOption();

  @ParametersDelegate
  private SchemaOption schema = new SchemaOption();

  public static void main(String[] args) throws Exception {
    Batch batch = new Batch();
    HelpOption help = help();
    JCommander commander = new JCommander(new Object[]{batch, help});
    commander.parse(args);
    help.check(commander);
    batch.run();
  }

  private void run() throws Exception {
    FineoClientBuilder builder = new FineoClientBuilder()
      .withApiKey(api.key)
      .withEndpoint(api.url)
      .withCredentials(api.credentials.get());

    try (BatchWrite batch = builder.build(BatchWrite.class)) {
      if (batchOpt.check) {
        batch.status(new URI(batchOpt.file));
      }
      // remote file to upload
      else if (batchOpt.file.startsWith("s3")) {
        BatchUploadRemoteS3File upload = new BatchUploadRemoteS3File();
        upload.setFilePath(batchOpt.file);
        batch.uploadS3File(upload);
      } else {
        // attempt to read the file based on the file ending and then upload that as a batch of
        // events
        String uploadFile = UUID.randomUUID().toString();
        System.out.println("Uploading file as: " + uploadFile);
        SingleStreamEventBase[] events = read(batchOpt.file);
        batch.write(uploadFile, events);
      }
    }
  }

  private SingleStreamEventBase[] read(String file) throws IOException, ClassNotFoundException {
    String type = file.substring(file.lastIndexOf(".") + 1);
    String name = schema.getName();
    Class<? extends SingleStreamEventBase> clazz = schema.getClazz();
    switch (type) {
      case "json":
        // read them out as an array of maps
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String>[] elements = mapper.readValue(new File(file), new
          TypeReference<Map<String, String>[]>() {});
        return convertEvents(Arrays.stream(elements), clazz, name);
      case "csv":
        CSVFormat format = CSVFormat.DEFAULT
          .withAllowMissingColumnNames(true)
          .withSkipHeaderRecord(false);
        Reader reader = new FileReader(file);
        return convertEvents(
          StreamSupport.stream(format.parse(reader).spliterator(), true).map(CSVRecord::toMap),
          clazz, name);
      default:
        throw new UnsupportedOperationException(
          "Batch read does not support local file type: " + type);
    }
  }

  private SingleStreamEventBase[] convertEvents(java.util.stream.Stream<Map<String, String>>
    stream, Class<? extends SingleStreamEventBase> clazz, String name) {
    ObjectMapper mapper = new ObjectMapper();
    return stream.map(map -> {
      try {
        return mapper.writeValueAsString(map);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }).map(msg -> {
      SingleStreamEventBase event = null;
      try {
        event = mapper.readValue(msg, clazz);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      setMetricType(event, clazz, name);
      return event;
    }).collect(Collectors.toList()).toArray(new SingleStreamEventBase[0]);
  }

  private void setMetricType(SingleStreamEventBase event,
    Class<? extends SingleStreamEventBase> clazz, String name) {
    if (name == null) {
      return;
    }
    try {
      Method m = clazz.getDeclaredMethod("setMetricType", String.class);
      m.invoke(event, name);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new IllegalArgumentException("Cannot set metric type name!", e);
    }
  }
}
