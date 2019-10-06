package org.ananas.runner.core.extension;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.ananas.runner.misc.HomeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.utils.IOUtils;

/**
 * ExtensionManager manages extensions of Ananas. It makes sure the job has all the required class
 * path from the extensions.
 *
 * <p>Each run will have its own extension manager to install and load extensions.
 */
public class ExtensionManager {
  private static final Logger LOG = LoggerFactory.getLogger(ExtensionManager.class);

  private static ExtensionManager INSTANCE = null;

  private static final String EXTENSION_FOLDER = "extensions";

  private Map<String, StepMetadata> stepMetadata;

  public ExtensionManager() {
    reset();
  }

  /**
   * Get the default extension manager.
   *
   * @return
   */
  public static ExtensionManager getDefault() {
    if (INSTANCE == null) {
      INSTANCE = new ExtensionManager();
    }
    return INSTANCE;
  }

  public void install(URL url, String name, String version, String repo) throws IOException {}

  /**
   * Install extension from a URI to a destination repo
   *
   * @param url
   * @param repo
   */
  private void install(URL url, String repo) throws IOException {
    BufferedInputStream in = new BufferedInputStream(url.openStream());

    // download the zip file
    File temp = File.createTempFile("ananas-ext", "");
    FileOutputStream fileOutputStream = new FileOutputStream(temp);
    byte dataBuffer[] = new byte[1024];
    int bytesRead;
    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
      fileOutputStream.write(dataBuffer, 0, bytesRead);
    }

    // unzip the file
    File destDir = new File(repo);
    byte[] buffer = new byte[1024];
    ZipInputStream zis = new ZipInputStream(new FileInputStream(temp));
    ZipEntry zipEntry = zis.getNextEntry();
    while (zipEntry != null) {
      File newFile = newFile(destDir, zipEntry);
      FileOutputStream fos = new FileOutputStream(newFile);
      int len;
      while ((len = zis.read(buffer)) > 0) {
        fos.write(buffer, 0, len);
      }
      fos.close();
      zipEntry = zis.getNextEntry();
    }
    zis.closeEntry();
    zis.close();
  }

  private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
    File destFile = new File(destinationDir, zipEntry.getName());

    String destDirPath = destinationDir.getCanonicalPath();
    String destFilePath = destFile.getCanonicalPath();

    if (!destFilePath.startsWith(destDirPath + File.separator)) {
      throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
    }

    return destFile;
  }

  /** Load extensions from default local extension repository */
  public void load() {
    load(HomeManager.getHomeFilePath(EXTENSION_FOLDER));
  }

  /**
   * Load extensions from extension repository
   *
   * @param extensionRoot
   */
  public void load(String extensionRoot) {
    loadStepExtensions(extensionRoot);
  }

  public Map<String, StepMetadata> getAllStepMetadata() {
    Map<String, StepMetadata> copy = new HashMap<>();
    this.stepMetadata.forEach(
        (k, v) -> {
          copy.put(k, v.clone());
        });
    return copy;
  }

  private void loadStepExtensions(String path) {
    LOG.info("Load extensions from repo" + path);
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

  /*
  private void loadAllExtensionVersions(String repo, String extension) {
    File extensionFolder = new File(repo + File.separator + extension);
    if (extensionFolder.exists() && extensionFolder.isDirectory()) {
      String[] versions = extensionFolder.list((file, name) -> {
        return new File(file, name).isDirectory();
      });

      for (String version : versions) {
        try {
          LOG.info("Load extension " + extension + " version " + version);
          loadExtension(new File(extensionFolder, version).getPath());
          succeed++;
        } catch (IOException e) {
          failed++;
          LOG.error(e.getLocalizedMessage());
          e.printStackTrace();
        }
      }
    }
  }
   */

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
                LOG.warn("Override metadata " + raw.id + " with extension: " + path);
                // allow override metadata here, so do not return
                // return;
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

  public void reset() {
    stepMetadata = new HashMap<>();
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
