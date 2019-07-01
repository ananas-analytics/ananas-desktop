package org.ananas.runner.legacy.steps.ml;

import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.legacy.core.StepConfig;
import org.ananas.runner.legacy.steps.filters.StepFilter;

public class IsStepTrainingMode implements StepFilter {

  public static IsStepTrainingMode of() {
    return new IsStepTrainingMode();
  }

  @Override
  public boolean filter(Step step) {
    String config = (String) step.getConfigParam(StepConfig.ML_MODE);
    return "train".equalsIgnoreCase(config) || "test".equalsIgnoreCase(config);
  }
}
