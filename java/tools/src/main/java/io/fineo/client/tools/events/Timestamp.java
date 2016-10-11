package io.fineo.client.tools.events;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tag a field as a timestamp field in a schema/event, rather than explicitly specifying the
 * timestamp as milliseconds since the epoch.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Timestamp {

  // Format of the timestamp field
  String[] pattern = new String[0];
}
