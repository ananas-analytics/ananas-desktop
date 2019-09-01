package org.ananas.runner.core.extension;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.net.URL;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EngineMetadata {
  public String id;
  public String name;
  public String version;
  public List<URL> classpath;
}
