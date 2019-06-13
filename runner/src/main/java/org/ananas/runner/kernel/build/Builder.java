package org.ananas.runner.kernel.build;

import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.ananas.runner.kernel.StepRunner;
import org.ananas.runner.kernel.model.Dag;
import org.ananas.runner.kernel.model.Dataframe;
import org.ananas.runner.kernel.model.Engine;
import org.ananas.runner.kernel.pipeline.PipelineContext;
import org.ananas.runner.kernel.model.TriggerOptions;
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
}
