package org.ananas.cli;

import org.ananas.cli.commands.MainCommand;
import org.ananas.runner.kernel.ExtensionRegistry;
import picocli.CommandLine;

public class Main {
  public static void main(String[] args) {
    ExtensionRegistry.init();

    int exitCode = new CommandLine(new MainCommand()).execute(args);

    if (exitCode
        >= 0) { // when runnng start server sub command, return -1 to avoid exit immediately
      System.exit(exitCode);
    }
  }
}
