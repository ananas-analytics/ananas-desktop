package org.ananas.runner.core.build;

import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.ananas.runner.core.StepRunner;
import org.ananas.runner.core.model.*;
import org.ananas.runner.core.pipeline.PipelineContext;
import org.apache.commons.lang3.tuple.MutablePair;

public interface Builder {

  /**
   * Build the intermediate structure and attach it to a speficified job
   *
   * @param jobId
   * @return
   */
  MutablePair<Map<String, StepRunner>, Stack<PipelineContext>> build(String jobId);

  Map<String, Dataframe> test();

  // get the goals
  Set<String> getGoals();

  Engine getEngine();

  Dag getDag();

  TriggerOptions getTrigger();

  Map<String, Variable> getParams();
}
