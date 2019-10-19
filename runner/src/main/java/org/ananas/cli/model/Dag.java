package org.ananas.cli.model;

import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import org.ananas.runner.core.model.DagEdge;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Dag {
  public Set<DagEdge> connections;

  public Dag() {
    connections = new HashSet<>();
  }
}
