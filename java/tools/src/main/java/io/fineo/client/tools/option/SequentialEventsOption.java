package io.fineo.client.tools.option;

import com.beust.jcommander.Parameter;

public class SequentialEventsOption {

  @Parameter(names = "--seq", description = "If the events should be sent sequentially (/event) "
                                            + "or as a batch (/events). By default sent as a batch")
  public boolean sequential = false;
}
