package io.fineo.client.tools.option;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.beust.jcommander.JCommander;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

public class TestSqlOption {

  @Test
  public void testCreateUrl() throws Exception {
    SqlOption option = new SqlOption();
    option.api = new ApiOption();
    option.api.key = "test-key";
    option.api.url = "http://someurl.com";
    CredentialsOption credentials = Mockito.mock(CredentialsOption.class);
    AWSCredentialsProvider provider =
      new AWSStaticCredentialsProvider(new BasicAWSCredentials("key", "secret"));
    Mockito.when(credentials.get()).thenReturn(provider);
    option.api.credentials = credentials;

    assertEquals("jdbc:fineo:url=http://someurl.com;api_key=test-key;authentication=static;"
                 + "aws_key=key;aws_secret=secret",
      option.getUrl());
  }

  @Test
  public void testGetQuery() throws Exception {
    SqlOption sql = new SqlOption();
    JCommander cmd = new JCommander(sql);
    cmd.parse("--api-key", "key", "SELECT", "*", "FROM", "table");
    assertEquals("SELECT * FROM table", sql.getQuery());
  }
}
