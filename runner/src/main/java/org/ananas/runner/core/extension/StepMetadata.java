package org.ananas.runner.core.extension;

import java.net.URL;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
public class StepMetadata {
  @Setter(AccessLevel.NONE)
  private String id;

  @Setter(AccessLevel.NONE)
  private String type;

  @Setter(AccessLevel.NONE)
  private List<URL> classpath;

  public StepMetadata(String id, String type, List<URL> classpath) {
    this.id = id;
    this.type = type;
    this.classpath = classpath;
  }
}
