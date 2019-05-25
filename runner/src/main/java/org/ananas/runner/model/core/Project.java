package org.ananas.runner.model.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Data;
import org.ananas.runner.kernel.model.Dag;
import org.ananas.runner.kernel.model.DagRequest;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.kernel.model.Variable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Project {

  public String id;
  public String name;
  public Dag dag;
  public Map<String, ProjectStep> steps;

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ProjectStep extends Step {

    public ProjectStep() {
      super();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Step)) {
        return false;
      }

      Step step = (Step) o;

      return this.id != null ? this.id.equals(step.id) : step.id == null;
    }

    public boolean deleted;
  }

  public DagRequest toDagRequest(String stepId, Map<String, Variable> params) {
    DagRequest req = new DagRequest();
    req.dag = new Dag();
    req.dag.connections = this.dag.connections;
    req.dag.steps = new HashSet<>();
    if (this.steps.size() > 0) {
      List<Step> collect =
          this.steps.values().stream().filter(e -> !e.deleted).collect(Collectors.toList());
      req.dag.steps.addAll(collect);
    }
    req.goals = new HashSet<>();
    req.goals.add(stepId);
    req.params = params;
    return req;
  }
}
