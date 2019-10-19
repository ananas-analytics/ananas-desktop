package org.ananas.cli.commands.extension;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.ananas.cli.Helper;
import org.ananas.runner.core.extension.LocalExtensionRepository;
import org.ananas.runner.core.model.Extension;
import org.ananas.runner.misc.HomeManager;
import org.ananas.runner.misc.YamlHelper;
import picocli.CommandLine;

@CommandLine.Command(name = "remove", description = "Remove installed extensions")
public class RemoveExtensionCommand implements Callable<Integer> {
  @CommandLine.Option(
      names = {"-p", "--project"},
      description = "Ananas analytics project path, default: current directory")
  private File project = new File(".");

  @CommandLine.Option(
      names = {"-r", "--repo"},
      description = "Extension repository location, by default, ./extensions")
  private File repo = new File("./extensions");

  @CommandLine.Option(
      names = {"-g", "--global"},
      description = "Remove the extension in global repository")
  private boolean global = false;

  @CommandLine.Parameters(
      description = "Extension to remove, in form of [name], or [name]@[version]. ")
  private List<String> extensions;

  @Override
  public Integer call() throws Exception {
    boolean isAnanasProject = Helper.isAnanasProject(project);
    if (!isAnanasProject && !global) {
      System.err.println(
          "Current directory is not a valid Ananas Project. Please use -p to specify Ananas Project, or use -g to install the extension in the global repository.");
      return 1;
    }

    if (global) {
      repo = new File(HomeManager.getHomeExtensionPath());
    }
    if (ExtensionHelper.initExtensionRepository(repo, Collections.emptyList()) != 0) {
      return 1;
    }

    Map<String, Extension> requiredExtensions = ExtensionHelper.getRequiredExtensions(project);

    for (String extension : extensions) {
      String[] parts = extension.split("@");
      if (parts.length == 1) {
        LocalExtensionRepository.getDefault().delete(parts[0], null);
        if (requiredExtensions.containsKey(parts[0])) {
          requiredExtensions.remove(parts[0]);
        }
      }
      if (parts.length >= 2) {
        LocalExtensionRepository.getDefault().delete(parts[0], parts[1]);
        if (requiredExtensions.containsKey(parts[0])) {
          Extension ext = requiredExtensions.get(parts[0]);
          if (ext.version.equals(parts[1])) {
            requiredExtensions.remove(parts[0]);
          }
        }
      }
    }

    if (isAnanasProject) {
      YamlHelper.saveYAML(new File(project, "extension.yml").getAbsolutePath(), requiredExtensions);
    }

    return 0;
  }
}
