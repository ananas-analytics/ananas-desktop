package org.ananas.cli.commands;

import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

@CommandLine.Command(name = "version", description = "Show CLI version")
public class VersionCommand implements Callable<Integer> {
  private static final Logger LOG = LoggerFactory.getLogger(VersionCommand.class);

  @Override
  public Integer call() throws Exception {
    String version = VersionCommand.class.getPackage().getImplementationVersion();
    if (version == null) {
      version = "development";
    }
    System.out.println(version);
    return 0;
  }
}
