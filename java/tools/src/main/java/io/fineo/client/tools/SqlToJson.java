package io.fineo.client.tools;

import com.beust.jcommander.JCommander;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fineo.client.tools.option.HelpOption;
import io.fineo.client.tools.option.SqlOption;
import io.fineo.read.Driver;
import org.apache.commons.dbutils.handlers.MapListHandler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static io.fineo.client.tools.option.HelpOption.help;

/**
 * Simple helper that converts the result of a SQL query against the Fineo API into a JSON object
 */
public class SqlToJson {

  public static void main(String[] args)
    throws IOException, SQLException {
    SqlOption sql = new SqlOption();
    HelpOption help = help();
    JCommander cmd = new JCommander(new Object[]{sql, help});
    cmd.setProgramName("SqlToJson");
    cmd.parse(args);
    help.check(cmd);

    Driver.load();
    List<Map<String, Object>> results;
    try (Connection conn = DriverManager.getConnection(sql.getUrl());
         ResultSet set = conn.createStatement().executeQuery(sql.getQuery())) {
      MapListHandler handler = new MapListHandler();
      results = handler.handle(set);
      ObjectMapper mapper = new ObjectMapper();
      String out = mapper.writeValueAsString(results);
      System.out.println(out);
    }
  }
}
