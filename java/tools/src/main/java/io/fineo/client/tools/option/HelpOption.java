package io.fineo.client.tools.option;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class HelpOption {
  @Parameter(names = {"-h", "--help"}, help = true)
  private boolean help;

  public static HelpOption help() {
    return new HelpOption();
  }

  public void check(JCommander commander) {
    if (!this.help) {
      return;
    }
    commander.usage();
    System.exit(0);
  }
}
