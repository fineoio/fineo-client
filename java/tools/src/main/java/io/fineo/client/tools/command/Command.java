package io.fineo.client.tools.command;

import io.fineo.client.FineoClientBuilder;

public interface Command {

  void run(FineoClientBuilder builder) throws Exception;
}
