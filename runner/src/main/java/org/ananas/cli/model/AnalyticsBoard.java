package org.ananas.cli.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.kernel.model.Variable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyticsBoard {
  public Dag dag;
  public Map<String, Step> steps;
  public Set<Variable> variables;

  public AnalyticsBoard() {
    dag = new Dag();
    steps = new HashMap<>();
    variables = new HashSet<>();
  }
}
