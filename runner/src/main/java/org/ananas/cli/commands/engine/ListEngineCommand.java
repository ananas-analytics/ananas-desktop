package org.ananas.cli.commands.engine;

import java.util.concurrent.Callable;
import picocli.CommandLine;

@CommandLine.Command(name = "list", description = "List installed engines")
public class ListEngineCommand implements Callable<Integer> {
  @Override
  public Integer call() throws Exception {
    // ExtensionManager.getInstance().loadExtensions();

    return 0;
  }
}
