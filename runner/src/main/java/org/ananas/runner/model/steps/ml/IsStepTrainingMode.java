package org.ananas.runner.model.steps.ml;

import org.ananas.runner.model.core.Step;
import org.ananas.runner.model.core.StepConfig;
import org.ananas.runner.model.steps.filters.StepFilter;

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
