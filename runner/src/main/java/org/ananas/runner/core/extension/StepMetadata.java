package org.ananas.runner.core.extension;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
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

  public StepMetadata clone() {
    List<URL> newClassPath = new ArrayList<>();
    Collections.copy(newClassPath, this.classpath);
    return new StepMetadata(this.id, this.type, newClassPath);
  }
}
