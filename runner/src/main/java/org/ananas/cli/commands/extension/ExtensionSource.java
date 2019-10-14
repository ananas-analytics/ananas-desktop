package org.ananas.cli.commands.extension;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

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
    source.version = version == null ? "latest" : version;
    source.resolved = false;

    if (path.startsWith("http:") || path.startsWith("https:")) {
      source.url = new URL(path);
      if (path.endsWith(".zip")) {
        source.resolved = true;
      }
    } else {
      // check if path points to a file
      if (path.contains("\\") || path.contains("/")) {
        File f = new File(path);
        source.url = Paths.get(f.toURI()).normalize().toUri().toURL();
        if (f.getAbsolutePath().endsWith(".zip")) {
          source.resolved = true;
        }
      } else {
        throw new MalformedURLException(
            "Invalid extension location: "
                + path
                + "@"
                + version
                + ". Please note that resolving extension name to its url is not supported yet, use extension url instead.");
      }
    }

    return source;
  }
}
