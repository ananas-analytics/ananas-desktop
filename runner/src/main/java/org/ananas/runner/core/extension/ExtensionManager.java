package org.ananas.runner.core.extension;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import org.ananas.runner.misc.HomeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.utils.IOUtils;

public class ExtensionManager {
  private static final Logger LOG = LoggerFactory.getLogger(ExtensionManager.class);

  private static ExtensionManager INSTANCE = null;

  private Map<String, StepMetadata> stepMetadata;
  private Map<String, EngineMetadata> engineMetadata;

  private ExtensionManager() {
    clean();
  }

  public static ExtensionManager getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ExtensionManager();
    }
    return INSTANCE;
  }

  public void loadExtensions() {
    String defaultExtensionPath = HomeManager.getHomeFilePath("extensions");
    File directory = new File(defaultExtensionPath);
    if (!directory.exists()) {
      directory.mkdirs();
    }
    loadExtensions(defaultExtensionPath);
  }

  public void loadExtensions(String path) {
    LOG.info("Load extensions from " + path);
    clean();
    File repo = new File(path);
    int succeed = 0;
    int failed = 0;
    if (repo.exists() && repo.isDirectory()) {
      String[] extensions =
          repo.list(
              (file, name) -> {
                return new File(file, name).isDirectory();
              });
      for (String ext : extensions) {
        try {
          LOG.info("Load extension " + ext);
          loadExtension(new File(path, ext).getPath());
          succeed++;
        } catch (IOException e) {
          failed++;
          LOG.error(e.getLocalizedMessage());
          e.printStackTrace();
        }
      }
    } else {
      LOG.error("Invalid extension repository: " + path);
    }
    LOG.info(
        "Load " + (succeed + failed) + " extensions, " + succeed + " succeed, " + failed + " fail");
  }

  public void loadExtension(String path) throws IOException {
    File extensionFolder = new File(path);
    File metadataFile = new File(path, "metadata.yml");
    Map<String, RawStepMetadata> rawMetadataMap =
        readRawStepMetadataFromFile(metadataFile.getPath());
    rawMetadataMap
        .values()
        .forEach(
            raw -> {
              StepMetadata meta = fromRawMetadata(extensionFolder, raw);
              if (meta == null) {
                LOG.warn("Invalid metadata: " + raw.id);
                return;
              }
              if (this.stepMetadata.containsKey(raw.id)) {
                LOG.warn(
                    "Ignore metadata " + raw.id + ", as there is already one with the same id");
                return;
              }
              this.stepMetadata.put(raw.id, meta);
            });
  }

  public boolean hasStepMetadata(String metadataId) {
    return this.stepMetadata.containsKey(metadataId);
  }

  public StepMetadata getStepMetadata(String metadataId) {
    return this.stepMetadata.get(metadataId);
  }

  public EngineMetadata getEngineMetadata(String engineId) {
    return this.engineMetadata.get(engineId);
  }

  private void clean() {
    stepMetadata = new HashMap<>();
    engineMetadata = new HashMap<>();
  }

  private StepMetadata fromRawMetadata(File extensionFile, RawStepMetadata rawMeta) {
    // list all libs
    File libsFolder = new File(extensionFile, "libs");
    File[] libs = libsFolder.listFiles();
    List<URL> classpath =
        Arrays.asList(libs).stream()
            .map(
                f -> {
                  try {
                    return f.toURI().toURL();
                  } catch (MalformedURLException e) {
                    e.printStackTrace();
                  }
                  return null;
                })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    return new StepMetadata(rawMeta.id, rawMeta.type, classpath);
  }

  private Map<String, RawStepMetadata> readRawStepMetadataFromFile(String path) throws IOException {
    FileInputStream in = new FileInputStream(path);
    String yaml = IOUtils.toString(in);

    TypeReference<Map<String, RawStepMetadata>> typeRef =
        new TypeReference<Map<String, RawStepMetadata>>() {};
    ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
    return yamlReader.readValue(yaml, typeRef);
  }
}
