package org.ananas.runner.kernel.build;

import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.ananas.runner.kernel.StepRunner;
import org.ananas.runner.kernel.model.Dataframe;
import org.ananas.runner.kernel.pipeline.PipelineContext;
import org.apache.commons.lang3.tuple.MutablePair;

public interface Builder {

  MutablePair<Map<String, StepRunner>, Stack<PipelineContext>> build();

  Map<String, Dataframe> test();

  // get the goals
  Set<String> getGoals();
}
