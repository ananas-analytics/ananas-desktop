package org.ananas.runner.core.extension;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import org.ananas.runner.core.errors.AnanasException;
import org.ananas.runner.core.errors.ExceptionHandler;
import org.ananas.runner.core.model.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.utils.IOUtils;

/**
 * ExtensionManager manages extensions of Ananas. It makes sure the job has all the required class
 * path from the extensions.
 *
 * <p>Each run will have its own extension manager to install and load extensions.
 */
public class DefaultExtensionManager implements ExtensionManager {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultExtensionManager.class);

  private Map<String, StepMetadata> stepMetadata;
  private ExtensionRepository repository;

  /**
   * Initiate ExtensionManager with specified ExtensionRepository
   *
   * @param repository
   */
  public DefaultExtensionManager(ExtensionRepository repository) {
    reset();
    this.repository = repository;
  }

  @Override
  public void resolve(Map<String, Extension> extensions) {
    if (extensions == null) {
      return;
    }
    extensions
        .entrySet()
        .forEach(
            entry -> {
              ExtensionManifest manifest =
                  this.repository.getExtension(entry.getKey(), entry.getValue().version);
              if (manifest == null) {
                throw new AnanasException(
                    ExceptionHandler.ErrorCode.EXTENSION,
                    "Can't resolve extension" + entry.getKey());
              }
              try {
                // FIXME: now we can suppose the URI is a file path
                loadExtension(manifest.getUri().toURL().getPath());
              } catch (Exception e) {
                LOG.error(
                    "Failed to load extension "
                        + entry.getKey()
                        + ". Reason: "
                        + e.getLocalizedMessage());
                throw new AnanasException(
                    ExceptionHandler.ErrorCode.EXTENSION, e.getLocalizedMessage());
              }
            });
  }

  @Override
  public Map<String, StepMetadata> getAllStepMetadata() {
    Map<String, StepMetadata> copy = new HashMap<>();
    this.stepMetadata.forEach(
        (k, v) -> {
          copy.put(k, v.clone());
        });
    return copy;
  }

  @Override
  public boolean hasStepMetadata(String metadataId) {
    return this.stepMetadata.containsKey(metadataId);
  }

  @Override
  public StepMetadata getStepMetadata(String metadataId) {
    return this.stepMetadata.get(metadataId);
  }

  @Override
  public void reset() {
    stepMetadata = new HashMap<>();
  }

  private void loadExtension(String path) throws IOException {
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
                LOG.warn("Override metadata " + raw.id + " with extension: " + path);
                // allow override metadata here, so do not return
                // return;
              }
              this.stepMetadata.put(raw.id, meta);
            });
  }

  private StepMetadata fromRawMetadata(File extensionFile, RawStepMetadata rawMeta) {
    // list all libs
    File libsFolder = new File(extensionFile, "lib");
    if (!libsFolder.exists()) {
      return new StepMetadata(rawMeta.id, rawMeta.type, new ArrayList<>());
    }
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

  private String getOrCreateDir(String root) {
    File directory = new File(root);
    if (!directory.exists()) {
      directory.mkdirs();
    }
    return directory.getPath();
  }
}
