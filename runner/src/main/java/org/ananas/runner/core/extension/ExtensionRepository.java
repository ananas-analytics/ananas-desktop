package org.ananas.runner.core.extension;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public interface ExtensionRepository {
  void load();

  void install(URL zip) throws IOException;

  void delete(String name, String version);

  boolean hasExtension(String name);

  boolean hasExtension(String name, String version);

  ExtensionManifest getExtension(String name);

  ExtensionManifest getExtension(String name, String version);

  List<String> getExtensionVersions(String name);
}
