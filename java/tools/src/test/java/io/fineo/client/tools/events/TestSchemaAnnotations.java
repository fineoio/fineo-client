package io.fineo.client.tools.events;


import org.junit.Test;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;

public class TestSchemaAnnotations {

  @Test
  public void testSimpleParsing() throws Exception {
    AnnotationAliases aliases = new AnnotationAliases(SchemaForTesting.class);
    assertEquals(newArrayList("elem"), aliases.getMetricTypeAliases());
    assertEquals(newArrayList("ts"), aliases.getTimestampAliases());
    assertEquals(newArrayList(SchemaForTesting.PATTERN), aliases.getTimestampPatterns());
  }

  /**
   * Schema can be null if there is no type specified
   * @throws Exception on error
   */
  @Test
  public void testNullSchema() throws Exception {
    AnnotationAliases aliases = new AnnotationAliases(null);
    assertEquals(newArrayList(), aliases.getTimestampAliases());
    assertEquals(newArrayList(), aliases.getTimestampPatterns());
    assertEquals(newArrayList(), aliases.getMetricTypeAliases());
  }
}
