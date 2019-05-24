package org.ananas.runner.model.steps.commons.build;

import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.ananas.runner.model.core.Dataframe;
import org.ananas.runner.model.core.PipelineContext;
import org.ananas.runner.model.steps.commons.StepRunner;
import org.apache.commons.lang3.tuple.MutablePair;

public interface Builder {

  MutablePair<Map<String, StepRunner>, Stack<PipelineContext>> build();

  Map<String, Dataframe> test();

  // get the goals
  Set<String> getGoals();
}
