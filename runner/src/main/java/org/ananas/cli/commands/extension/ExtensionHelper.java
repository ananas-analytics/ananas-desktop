package org.ananas.cli.commands.extension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.ananas.runner.core.extension.DefaultExtensionManager;
import org.ananas.runner.core.extension.ExtensionManager;
import org.ananas.runner.core.extension.LocalExtensionRepository;
import org.ananas.runner.core.model.Extension;
import org.ananas.runner.misc.YamlHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtensionHelper {
  private static final Logger LOG = LoggerFactory.getLogger(ExtensionHelper.class);

  public static ExtensionManager initExtensionManager(
      File project, File repo, List<File> extensions) {
    if (initExtensionRepository(repo, extensions) != 0) {
      return null;
    }
    ExtensionManager extensionManager = initExtensionManagerWithLocalRepository(project);
    return extensionManager;
  }

  public static int initExtensionRepository(File repo, List<File> extensions) {
    if (repo != null) {
      if (!repo.exists()) {
        repo.mkdirs();
      }
      LocalExtensionRepository.setDefaultRepository(repo.getAbsolutePath());
    }
    LocalExtensionRepository.getDefault().load();

    // add additional extensions to repository
    AtomicInteger ret = new AtomicInteger();
    if (extensions != null) {
      extensions.forEach(
          ext -> {
            try {
              LocalExtensionRepository.getDefault().addExtension(ext);
            } catch (IOException e) {
              System.err.println("Failed to add extension to repository: " + ext.getAbsolutePath());
              ret.set(1);
            }
          });
    }
    return ret.get();
  }

  public static Map<String, Extension> getRequiredExtensions(File project) {
    // parse extension.yml
    File extensionFile = Paths.get(project.getAbsolutePath(), "extension.yml").toFile();
    if (extensionFile.exists()) {
      Map<String, Extension> extensions = null;
      try {
        extensions = YamlHelper.openMapYAML(extensionFile.getAbsolutePath(), Extension.class);
        return extensions;
      } catch (IOException e) {
        LOG.error("Failed to parse extension file: " + e.getLocalizedMessage());
      }
    }
    return new HashMap<>();
  }

  public static ExtensionManager initExtensionManagerWithLocalRepository(File project) {
    Map<String, Extension> requiredExtensions = getRequiredExtensions(project);
    ExtensionManager extensionManager =
        new DefaultExtensionManager(LocalExtensionRepository.getDefault());
    extensionManager.resolve(requiredExtensions);
    return extensionManager;
  }
}
