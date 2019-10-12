package org.ananas.runner.core.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Extension {
  public String version;
  public String resolved;
  public String checksum;

  public Extension(String version, String resolved, String checksum) {
    this.version = version;
    this.resolved = resolved;
    this.checksum = checksum;
  }
}
