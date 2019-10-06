package org.ananas.runner.core.extension;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/** ExtensionDescriptor contains the URL of the resources in Extension */
public class ExtensionManifest {
  private URL descriptor;
  private URL metadata;
  private List<URL> editors;
  private List<URL> libs;

  public ExtensionManifest(URL descriptor, URL metadata, List<URL> editor, List<URL> libs) {
    this.descriptor = descriptor;
    this.metadata = metadata;
    this.editors = editor == null ? new ArrayList<>() : editor;
    this.libs = libs == null ? new ArrayList<>() : libs;
  }

  public URL getDescriptor() {
    return descriptor;
  }

  public URL getMetadata() {
    return metadata;
  }

  public List<URL> getEditors() {
    return new ArrayList<>(editors);
  }

  public List<URL> getLibs() {
    return new ArrayList<>(libs);
  }
}
