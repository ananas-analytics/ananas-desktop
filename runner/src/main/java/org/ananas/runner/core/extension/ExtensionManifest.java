package org.ananas.runner.core.extension;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/** ExtensionDescriptor contains the URL of the resources in Extension */
public class ExtensionManifest {
  private URI uri; // uri to identify the extension, injected by the repository
  private URL descriptor;
  private URL metadata;
  private List<URL> editors;
  private List<URL> libs;

  public URI getUri() {
    return uri;
  }

  public ExtensionManifest(
      URI uri, URL descriptor, URL metadata, List<URL> editor, List<URL> libs) {
    this.uri = uri;
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
