package org.ananas.runner.core.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Dag {

  public Set<DagEdge> connections;
  public Set<Step> steps;

  @Override
  public String toString() {
    return "Dag{" + "connections=" + this.connections + ", steps=" + this.steps + '}';
  }

  public void setConnections(Set<DagEdge> connections) {
    this.connections = connections;
  }

  public Dag(Set<DagEdge> connections, Set<Step> steps) {
    this.connections = connections;
    this.steps = steps;
  }

  public Dag copy() {
    return new Dag(new HashSet<>(this.connections), new HashSet<>(this.steps));
  }

  public Dag() {
    this.connections = new HashSet<>();
    this.steps = new HashSet<>();
  }

  public Set<DagEdge> getConnections() {
    return this.connections;
  }

  public Set<Step> getSteps() {
    return this.steps;
  }
}
