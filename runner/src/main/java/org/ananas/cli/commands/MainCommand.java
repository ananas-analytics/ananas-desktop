package org.ananas.cli.commands;

import ch.qos.logback.classic.Level;
import java.util.concurrent.Callable;
import org.ananas.cli.commands.extension.ExtensionCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Option;

@Command(
    name = "ananas",
    description = "Ananas Analytics Command Line Interface",
    subcommands = {
      ExploreCommand.class,
      ExtensionCommand.class,
      RunCommand.class,
      ShowCommand.class,
      StartCommand.class,
      TestCommand.class,
      VersionCommand.class,
      ViewCommand.class,
      HelpCommand.class,
    })
public class MainCommand implements Callable<Integer> {
  private static final Logger LOG = LoggerFactory.getLogger(MainCommand.class);

  @Option(
      names = {"-v", "--verbose"},
      description = "turn on (v)erbose mode")
  private boolean verbose;

  @Override
  public Integer call() throws Exception {
    handleVerbose();
    CommandLine.usage(this, System.out);
    return 0;
  }

  public void handleVerbose() {
    ch.qos.logback.classic.Logger rootLogger =
        (ch.qos.logback.classic.Logger)
            LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
    rootLogger.detachAppender("STDOUT");
    if (!verbose) {
      rootLogger.detachAppender("CLI");
    } else {
      rootLogger.setLevel(Level.INFO);
    }
  }
}
