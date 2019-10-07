package org.ananas.runner.core.extension;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.ananas.runner.misc.YamlHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalExtensionRepository implements ExtensionRepository {
  private static final Logger LOG = LoggerFactory.getLogger(LocalExtensionRepository.class);
  private File root;
  private Map<String, Map<String, ExtensionManifest>> cache;

  public LocalExtensionRepository(String root) {
    this.root = new File(root);
    this.cache = new HashMap<>();
  }

  @Override
  public void load() {
    String[] extensions =
        getDirectoryContents(root, (file, name) -> new File(file, name).isDirectory());
    for (String ext : extensions) {
      String[] versions =
          getDirectoryContents(
              new File(root, ext), (file, name) -> new File(file, name).isDirectory());
      for (String ver : versions) {
        ExtensionManifest manifest = loadExtension(ext, ver);
        if (manifest != null) {
          addToCache(ext, ver, manifest);
        }
      }
    }
  }

  private void addToCache(String name, String version, ExtensionManifest manifest) {
    if (!cache.containsKey(name)) {
      cache.put(name, new HashMap<String, ExtensionManifest>());
    }
    Map<String, ExtensionManifest> extVersions = cache.get(name);
    extVersions.put(version, manifest);
  }

  @Override
  public void install(URL url) throws IOException {
    // unzip the file
    File destDir = unzip(url);

    // load extension manifest
    ExtensionManifest manifest = loadExtensionManifest(destDir);
    if (manifest == null) {
      throw new IOException("Invalid extension");
    }

    InputStream descIn = manifest.getDescriptor().openStream();

    ExtensionDescriptor descriptor = YamlHelper.openYAML(descIn, ExtensionDescriptor.class);

    if (descriptor.name == null || descriptor.version == null) {
      throw new IOException("Invalid extension descriptor");
    }
    // copy the extension from the temp folder to the extension folder
    File extFolder = new File(root, descriptor.name);
    File verFolder = new File(extFolder, descriptor.version);
    if (!verFolder.exists()) {
      verFolder.mkdirs();
    }

    FileUtils.copyDirectory(destDir, verFolder);

    ExtensionManifest newManifest = loadExtensionManifest(verFolder);
    addToCache(descriptor.name, descriptor.version, newManifest);

    destDir.delete();
  }

  @Override
  public void delete(String name, String version) {
    if (!cache.containsKey(name)) {
      return;
    }
    Map<String, ExtensionManifest> extVersions = cache.get(name);
    if (!extVersions.containsKey(version)) {
      return;
    }
    extVersions.remove(version);
  }

  @Override
  public boolean hasExtension(String name) {
    if (!cache.containsKey(name)) {
      return false;
    }
    Map<String, ExtensionManifest> versions = cache.get(name);
    return versions.size() > 0;
  }

  @Override
  public boolean hasExtension(String name, String version) {
    if (!cache.containsKey(name)) {
      return false;
    }
    Map<String, ExtensionManifest> versions = cache.get(name);
    if (!versions.containsKey(version)) {
      return false;
    }
    return true;
  }

  @Override
  public ExtensionManifest getExtension(String name, String version) {
    if (!hasExtension(name, version)) {
      return null;
    }
    Map<String, ExtensionManifest> versions = cache.get(name);
    return versions.get(version);
  }

  @Override
  public List<String> getExtensionVersions(String name) {
    if (!cache.containsKey(name)) {
      return Collections.emptyList();
    }
    return cache.get(name).keySet().stream().collect(Collectors.toList());
  }

  /**
   * get the highest version of extension that matches the extension version requirement
   *
   * @param name the name of the extension
   * @return
   */
  @Override
  public ExtensionManifest getExtension(String name) {
    if (!cache.containsKey(name)) {
      return null;
    }
    Map<String, ExtensionManifest> versions = cache.get(name);

    String ananasVersion = LocalExtensionRepository.class.getPackage().getImplementationVersion();
    if (ananasVersion == null) {
      ananasVersion = "0.0.0";
    }

    // resolve extension version with minAnanasVersion
    String current = "0.0.0";
    // versions.keySet().

    return null;
  }

  private File unzip(URL url) throws IOException {
    BufferedInputStream in = new BufferedInputStream(url.openStream());

    // download the zip file
    File temp = File.createTempFile("ananas-ext", "");
    FileOutputStream fileOutputStream = new FileOutputStream(temp);
    byte dataBuffer[] = new byte[1024];
    int bytesRead;
    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
      fileOutputStream.write(dataBuffer, 0, bytesRead);
    }
    in.close();

    // unzip the file
    File destDir = Files.createTempDirectory("ananas-ext").toFile();
    ZipFile zip = new ZipFile(temp);
    Enumeration<? extends ZipEntry> entries = zip.entries();
    while (entries.hasMoreElements()) {
      ZipEntry entry = entries.nextElement();
      Path entryPath = destDir.toPath().resolve(entry.getName());
      if (entry.isDirectory()) {
        Files.createDirectories(entryPath);
      } else {
        Files.createDirectories(entryPath.getParent());
        try (InputStream ins = zip.getInputStream(entry)) {
          try (OutputStream out = new FileOutputStream(entryPath.toFile())) {
            IOUtils.copy(ins, out);
          }
        }
      }
    }

    return destDir;
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

  private ExtensionManifest loadExtension(String name, String version) {
    File extensionFolder = new File(root, name);
    if (!extensionFolder.exists()) {
      return null;
    }

    File versionFolder = new File(extensionFolder, version);
    if (!versionFolder.exists()) {
      return null;
    }

    return loadExtensionManifest(versionFolder);
  }

  private ExtensionManifest loadExtensionManifest(File path) {
    File descriptor = new File(path, "extension.yml");
    if (!descriptor.exists()) {
      LOG.error("Can't find extension.yml from " + path);
      return null;
    }
    URL descriptorURL, metadataURL = null;
    try {
      descriptorURL = descriptor.toURI().toURL();
    } catch (MalformedURLException e) {
      LOG.error("Invalid extension descriptor URL: " + descriptor.getAbsolutePath());
      return null;
    }

    File metadata = new File(path, "metadata.yml");
    if (!metadata.exists()) {
      LOG.error("Can't load metadata.yml from " + path);
      return null;
    }
    try {
      metadataURL = metadata.toURI().toURL();
    } catch (MalformedURLException e) {
      LOG.error("Invalid metadata URL: " + metadata.getAbsolutePath());
      return null;
    }

    List<URL> editorUrls = new ArrayList<>();
    File editors = new File(path, "editor");
    String[] editorMetadata =
        getDirectoryContents(editors, (file, name) -> new File(file, name).isFile());
    for (String meta : editorMetadata) {
      try {
        editorUrls.add(new File(editors, meta).toURI().toURL());
      } catch (MalformedURLException e) {
        LOG.warn("Failed to load editor " + meta + " from path " + editors.getAbsolutePath());
      }
    }

    List<URL> libUrls = new ArrayList<>();
    File libs = new File(path, "lib");
    String[] libArray = getDirectoryContents(libs, (file, name) -> new File(file, name).isFile());
    for (String lib : libArray) {
      try {
        libUrls.add(new File(libs, lib).toURI().toURL());
      } catch (MalformedURLException e) {
        LOG.warn("Failed to load lib " + lib + " from path " + libs.getAbsolutePath());
      }
    }

    ExtensionManifest extDesc =
        new ExtensionManifest(descriptorURL, metadataURL, editorUrls, libUrls);
    return extDesc;
  }

  private String[] getDirectoryContents(File path, FilenameFilter filter) {
    if (path.exists() && path.isDirectory()) {
      String[] folders = path.list(filter);
      return folders;
    }
    return new String[] {};
  }
}
