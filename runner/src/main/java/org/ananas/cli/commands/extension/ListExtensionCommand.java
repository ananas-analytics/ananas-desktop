package org.ananas.cli.commands.extension;

import java.util.concurrent.Callable;
import picocli.CommandLine;

@CommandLine.Command(name = "list", description = "List installed extensions")
public class ListExtensionCommand implements Callable<Integer> {
  @Override
  public Integer call() throws Exception {
    return 0;
  }
}
