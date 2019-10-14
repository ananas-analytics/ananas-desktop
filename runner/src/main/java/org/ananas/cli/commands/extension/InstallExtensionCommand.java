package org.ananas.cli.commands.extension;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.ananas.cli.Helper;
import org.ananas.runner.core.extension.ExtensionDescriptor;
import org.ananas.runner.core.extension.ExtensionManifest;
import org.ananas.runner.core.extension.LocalExtensionRepository;
import org.ananas.runner.core.model.Extension;
import org.ananas.runner.misc.HomeManager;
import org.ananas.runner.misc.YamlHelper;
import picocli.CommandLine;

@CommandLine.Command(name = "install", description = "Install extension")
public class InstallExtensionCommand implements Callable<Integer> {
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
      description = "Install the extension to global repository")
  private boolean global = false;

  @CommandLine.Parameters(
      description = "Extension to install, in form of [name|url], or [name|url]@[version]. ")
  private List<String> extensions;

  @Override
  public Integer call() throws Exception {
    if (global) {
      repo = new File(HomeManager.getHomeExtensionPath());
    }
    ExtensionHelper.initExtensionRepository(repo, Collections.emptyList());

    boolean isAnanasProject = Helper.isAnanasProject(project);

    if (!isAnanasProject && !global) {
      System.err.println(
          "Current directory is not a valid Ananas Project. Please use -p to specify Ananas Project, or use -g to install the extension in the global repository.");
      return 1;
    }

    Map<String, Extension> requiredExtensions = ExtensionHelper.getRequiredExtensions(project);

    int success = 0;
    int fail = 0;
    for (String extension : extensions) {
      System.out.println("Installing " + extension);
      try {
        ExtensionSource source = ExtensionSource.parse(extension);

        if (source.resolved) {
          ExtensionManifest manifest = LocalExtensionRepository.getDefault().publish(source.url);
          ExtensionDescriptor descriptor =
              YamlHelper.openYAML(manifest.getDescriptor().openStream(), ExtensionDescriptor.class);
          System.out.println(
              "Resolve extension to " + descriptor.name + " version " + descriptor.version);

          Extension ext = new Extension(descriptor.version, source.url.toString(), null);
          requiredExtensions.put(descriptor.name, ext);
        } else {

        }
        success++;
      } catch (IOException e) {
        System.err.println(e.getLocalizedMessage());
        fail++;
      }
    }

    if (isAnanasProject) {
      YamlHelper.saveYAML(new File(project, "extension.yml").getAbsolutePath(), requiredExtensions);
    }

    System.out.println();
    System.out.println(
        "Installed "
            + (success + fail)
            + " extensions. "
            + success
            + " Success, "
            + fail
            + " Fail.");
    return 0;
  }
}