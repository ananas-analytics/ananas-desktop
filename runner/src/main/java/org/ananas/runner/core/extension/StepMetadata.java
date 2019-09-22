package org.ananas.runner.core.extension;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
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
    this.classpath.forEach(url -> newClassPath.add(url));
    return new StepMetadata(this.id, this.type, newClassPath);
  }
}
