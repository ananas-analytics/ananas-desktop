package org.ananas.cli.commands;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import org.ananas.cli.commands.extension.ExtensionHelper;
import org.ananas.runner.misc.HomeManager;
import org.ananas.server.RestApiRoutes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
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

  @Option(
      names = {"-r", "--repo"},
      description = "Extension repository location, by default, ./extensions")
  private File repo = new File("./extensions");

  @CommandLine.Option(
      names = {"-g", "--global"},
      description = "Load extensions from global repository")
  private boolean global = false;

  @Option(
      names = {"-x", "--extension"},
      description = "Extension location, could be absolute path or relative to current directory")
  private List<File> extensions;

  @Override
  public Integer call() throws Exception {
    parent.handleVerbose();

    if (global) {
      repo = new File(HomeManager.getHomeExtensionPath());
    }
    if (ExtensionHelper.initExtensionRepository(repo, extensions) != 0) {
      return 1;
    }

    System.out.printf("Server started at %s, port %d", host, port);
    RestApiRoutes.initRestApi(host, port);
    return -1; // return -1 to avoid exit immediately
  }
}
