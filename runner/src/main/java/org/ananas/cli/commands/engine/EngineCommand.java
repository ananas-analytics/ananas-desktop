package org.ananas.cli.commands.engine;

import java.util.concurrent.Callable;
import picocli.CommandLine;

@CommandLine.Command(
    name = "engine",
    description = "Execution engine related commands",
    subcommands = {
      InstallEngineCommand.class,
      ListEngineCommand.class,
    })
public class EngineCommand implements Callable<Integer> {
  @Override
  public Integer call() throws Exception {
    CommandLine.usage(this, System.out);
    return 0;
  }
}
