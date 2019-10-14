package org.ananas.cli.commands.extension;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.FileInputStream;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.utils.IOUtils;

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
    File extensionFile = Paths.get(project.getAbsolutePath(), "extension.yml").toFile();
    Map<String, Extension> extensions = new HashMap<>();
    if (extensionFile.exists()) {
      try {
        return ExtensionHelper.openExtensionList(extensionFile.getAbsolutePath());
      } catch (IOException e) {
        LOG.error("Failed to parse extension file: " + e.getLocalizedMessage());
      }
    }
    return extensions;

    /*
    // parse extension.yml
    File extensionFile = Paths.get(project.getAbsolutePath(), "extension.yml").toFile();
    Map<String, Extension> extensions = new HashMap<>();
    if (extensionFile.exists()) {
      try {
        extensions = YamlHelper.openMapYAML(extensionFile.getAbsolutePath(), Extension.class);
        return extensions;
      } catch (IOException e) {
        LOG.error("Failed to parse extension file: " + e.getLocalizedMessage());
      }
    }
    return extensions;
     */
  }

  public static ExtensionManager initExtensionManagerWithLocalRepository(File project) {
    Map<String, Extension> requiredExtensions = getRequiredExtensions(project);
    ExtensionManager extensionManager =
        new DefaultExtensionManager(LocalExtensionRepository.getDefault());
    extensionManager.resolve(requiredExtensions);
    return extensionManager;
  }

  public static Map<String, Extension> openExtensionList(String path) throws IOException {
    FileInputStream in = new FileInputStream(path);
    String yaml = IOUtils.toString(in);
    TypeReference<HashMap<String, Extension>> typeRef =
        new TypeReference<HashMap<String, Extension>>() {};
    ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
    return yamlReader.readValue(yaml, typeRef);
  }
}
