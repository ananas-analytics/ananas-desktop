package org.ananas.cli;

import org.ananas.cli.commands.MainCommand;
import org.ananas.runner.kernel.ExtensionRegistry;
import picocli.CommandLine;

public class Main {
  public static void main(String[] args) {
    ExtensionRegistry.init();

    int code = CommandLine.call(new MainCommand(), args);

    System.exit(code);
  }
}
