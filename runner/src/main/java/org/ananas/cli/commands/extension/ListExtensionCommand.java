package org.ananas.cli.commands.extension;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.ananas.runner.core.common.JsonUtil;
import org.ananas.runner.core.extension.ExtensionManifest;
import org.ananas.runner.core.extension.LocalExtensionRepository;
import org.ananas.runner.misc.HomeManager;
import picocli.CommandLine;

@CommandLine.Command(name = "list", description = "List installed extensions")
public class ListExtensionCommand implements Callable<Integer> {

  @CommandLine.Option(
      names = {"-r", "--repo"},
      description = "Extension repository location, by default, ./extensions")
  private File repo = new File("./extensions");

  @CommandLine.Option(
      names = {"-g", "--global"},
      description = "List extensions from global repository")
  private boolean global = false;

  @CommandLine.Option(
      names = {"-x", "--extension"},
      description = "Extension location, could be absolute path or relative to current directory")
  private List<File> extensions;

  @Override
  public Integer call() throws Exception {
    if (global) {
      repo = new File(HomeManager.getHomeExtensionPath());
    }
    if (ExtensionHelper.initExtensionRepository(repo, extensions) != 0) {
      return 1;
    }

    Map<String, Map<String, ExtensionManifest>> result =
        LocalExtensionRepository.getDefault().getExtensions();
    System.out.println(JsonUtil.toJson(result));
    return 0;
  }
}
