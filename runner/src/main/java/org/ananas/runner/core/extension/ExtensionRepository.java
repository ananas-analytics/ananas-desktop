package org.ananas.runner.core.extension;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public interface ExtensionRepository {
  /**
   * Get the root path of the repository
   *
   * @return the repository path
   */
  String getRepositoryRoot();

  /** Load extensions into cache */
  void load();

  /**
   * publish an extension to the repo
   *
   * @param zip the URL of the extension zip file
   * @returns the extension manifest
   * @throws IOException
   */
  ExtensionManifest publish(URL zip) throws IOException;

  /**
   * Extract the extension
   *
   * @param zip the URL of the extension zip file
   * @return the extension manifest
   * @throws IOException
   */
  ExtensionManifest extract(URL zip) throws IOException;

  /**
   * Add additional extension into the repository from a folder.
   *
   * <p>The difference between publish and addExtension is: publish accepts a zip resource
   * addExtension accepts a unziped resource
   *
   * @param path the unziped extension
   * @return extension manifest or null
   */
  ExtensionManifest addExtension(File path) throws IOException;
  /**
   * Delete one version of extension
   *
   * @param name the extension name
   * @param version the version of the extension
   */
  void delete(String name, String version) throws IOException;

  /**
   * Get extensions and all its versions
   *
   * @return
   */
  Map<String, Map<String, ExtensionManifest>> getExtensions();

  /**
   * Check if an extension exists in the repository
   *
   * @param name the name of the extension
   * @return true if exists, otherwise false
   */
  boolean hasExtension(String name);

  /**
   * Check if a specific version of extension exists in the repository
   *
   * @param name the name of the extension
   * @param version the extension version
   * @return true if exists, otherwise false
   */
  boolean hasExtension(String name, String version);

  /**
   * Get the extension from name If there are multiple versions, the latest is returned
   *
   * @param name extension name
   * @return the manifest of the extension
   */
  ExtensionManifest getExtension(String name);

  /**
   * Get the extension from its name and version
   *
   * @param name extension name
   * @param version extension version
   * @return the extension manifest or null
   */
  ExtensionManifest getExtension(String name, String version);

  /**
   * Get all the available versions of the extension
   *
   * @param name extension name
   * @return a list of versions
   */
  List<String> getExtensionVersions(String name);
}
