package org.ananas.cli.commands;

import java.util.concurrent.Callable;
import org.ananas.runner.api.RestApiRoutes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Command(name = "start", description = "Run Ananas Server")
public class StartCommand implements Callable<Integer> {
  private static final Logger LOG = LoggerFactory.getLogger(StartCommand.class);

  @ParentCommand private MainCommand parent;

  @Option(
      names = {"-h", "--host"},
      description = "Server host, default localhost",
      defaultValue = "127.0.0.1")
  private String host;

  @Option(
      names = {"-p", "--port"},
      description = "Server port, default 3003",
      defaultValue = "3003")
  private Integer port;

  @Override
  public Integer call() throws Exception {
    parent.handleVerbose();

    System.out.printf("Server started at %s, port %d", host, port);
    RestApiRoutes.initRestApi(new String[] {host, port.toString()});
    return -1; // return -1 to avoid exit immediately
  }
}
