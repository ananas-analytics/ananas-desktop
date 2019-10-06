package org.ananas.cli.commands.extension;

import java.util.concurrent.Callable;
import picocli.CommandLine;

@CommandLine.Command(name = "install", description = "Install extension")
public class InstallExtensionCommand implements Callable<Integer> {
  @CommandLine.Option(
      names = {"-g", "--global"},
      description = "Install the extension to global repository")
  private boolean global = false;

  @CommandLine.Parameters(
      index = "0",
      arity = "0..1",
      description = "Extension to install, in form of [name|url], or [name|url]@[version]. ")
  private String extension;

  @Override
  public Integer call() throws Exception {
    if (extension == null) {

    } else {

    }
    return 0;
  }
}
