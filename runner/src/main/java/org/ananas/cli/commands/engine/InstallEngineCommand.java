package org.ananas.cli.commands.engine;

import java.util.concurrent.Callable;
import picocli.CommandLine;

@CommandLine.Command(name = "install", description = "Install engine")
public class InstallEngineCommand implements Callable<Integer> {
  @Override
  public Integer call() throws Exception {
    return 0;
  }
}
