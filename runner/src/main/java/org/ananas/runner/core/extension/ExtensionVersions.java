package org.ananas.runner.core.extension;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.zafarkhaja.semver.Version;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExtensionVersions {
  public String name;
  public List<ExtensionDescriptor> versions;

  public static void sortVersions(List<ExtensionDescriptor> versions) {
    versions.sort((a, b) -> Version.valueOf(b.version).compareTo(Version.valueOf(a.version)));
  }
}
