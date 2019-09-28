package org.ananas.cli.commands.extension;

import java.util.concurrent.Callable;
import picocli.CommandLine;

@CommandLine.Command(name = "install", description = "Install extension")
public class InstallExtensionCommand implements Callable<Integer> {

  @Override
  public Integer call() throws Exception {
    return 0;
  }
}
