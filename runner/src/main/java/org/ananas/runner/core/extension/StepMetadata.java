package org.ananas.runner.core.extension;

import java.net.URL;
import java.util.List;

public class StepMetadata {
  public String id;
  public String type;
  public List<URL> classpath;

  public StepMetadata(String id, String type, List<URL> classpath) {
    this.id = id;
    this.type = type;
    this.classpath = classpath;
  }
}
