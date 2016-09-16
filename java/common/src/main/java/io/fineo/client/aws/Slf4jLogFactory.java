package io.fineo.client.aws;

import com.amazonaws.log.InternalLogApi;
import com.amazonaws.log.InternalLogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.valueOf;

/**
 * Factory for AWS client logging that uses SLF4J as the log maker.
 */
public class Slf4jLogFactory extends InternalLogFactory {

  static {
    InternalLogFactory.configureFactory(new Slf4jLogFactory());
  }

  public static boolean enable() {
    return true;
  }

  @Override
  protected InternalLogApi doGetLog(Class<?> clazz) {
    return new Slf4jLog(clazz);
  }

  @Override
  protected InternalLogApi doGetLog(String name) {
    return new Slf4jLog(name);
  }

  private static class Slf4jLog implements InternalLogApi {

    private final Logger log;

    public Slf4jLog(String name) {
      this.log = LoggerFactory.getLogger(name);
    }

    public Slf4jLog(Class<?> clazz) {
      this.log = LoggerFactory.getLogger(clazz);
    }

    @Override
    public void debug(Object message) {
      log.debug(valueOf(message));
    }

    @Override
    public void debug(Object message, Throwable t) {
      log.debug(valueOf(message), t);
    }

    @Override
    public void error(Object message) {
      log.error(valueOf(message));
    }

    @Override
    public void error(Object message, Throwable t) {
      log.error(valueOf(message), t);
    }

    @Override
    public void fatal(Object message) {
      log.error("FATAL - " + valueOf(message));
    }

    @Override
    public void fatal(Object message, Throwable t) {
      log.error("FATAL - " + valueOf(message), t);
    }

    @Override
    public void info(Object message) {
      log.info(valueOf(message));
    }

    @Override
    public void info(Object message, Throwable t) {
      log.debug(valueOf(message), t);
    }

    @Override
    public boolean isDebugEnabled() {
      return log.isDebugEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
      return log.isErrorEnabled();
    }

    @Override
    public boolean isFatalEnabled() {
      return log.isErrorEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
      return log.isInfoEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
      return log.isTraceEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
      return log.isWarnEnabled();
    }

    @Override
    public void trace(Object message) {
      log.trace(valueOf(message));
    }

    @Override
    public void trace(Object message, Throwable t) {
      log.trace(valueOf(message), t);
    }

    @Override
    public void warn(Object message) {
      log.warn(valueOf(message));
    }

    @Override
    public void warn(Object message, Throwable t) {
      log.warn(valueOf(message), t);
    }
  }
}
