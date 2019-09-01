package org.ananas.runner.core.extension;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RawStepMetadata {
  public String id;
  public String type;
}
