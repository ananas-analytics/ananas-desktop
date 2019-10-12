package org.ananas.cli.commands.extension;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class ExtensionSource {
  public URL url;
  public String version;
  // Whether the source url is resolved to the final download url
  public boolean resolved;

  public static ExtensionSource parse(String path) throws MalformedURLException {
    if (path == null) {
      throw new MalformedURLException("Invalid extension location: null");
    }

    String[] parts = path.split("@");

    if (parts.length == 1) {
      return parseURL(path, "latest");
    }

    if (parts.length >= 2) {
      return parseURL(parts[0], parts[1]);
    }

    throw new MalformedURLException("Invalid extension location: " + path);
  }

  private static ExtensionSource parseURL(String path, String version)
      throws MalformedURLException {
    ExtensionSource source = new ExtensionSource();

    if (path.endsWith(".zip")) {
      source.resolved = true;
    } else {
      source.resolved = false;
    }

    // handle relative path
    if (path.startsWith(".")) {
      path = "file:" + new File(path).getAbsolutePath();
    }

    if (path.startsWith("http:") || path.startsWith("file:")) {
      source.url = new URL(path);
    } else {
      // TODO: resolve the url from the extension name
      throw new MalformedURLException(
          "Invalid extension location: "
              + path
              + "@"
              + version
              + ". Please note that resolving extension name to its url is not supported yet, use extension url instead.");
    }
    source.version = version == null ? "latest" : version;

    return source;
  }
}
