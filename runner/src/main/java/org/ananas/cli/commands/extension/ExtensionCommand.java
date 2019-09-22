package org.ananas.cli.commands.extension;

import java.util.concurrent.Callable;
import picocli.CommandLine;

@CommandLine.Command(
    name = "extension",
    description = "Extension related commands",
    subcommands = {
      InstallExtensionCommand.class,
      ListExtensionCommand.class,
      CommandLine.HelpCommand.class,
    })
public class ExtensionCommand implements Callable<Integer> {
  @Override
  public Integer call() throws Exception {

    return 0;
  }
}
