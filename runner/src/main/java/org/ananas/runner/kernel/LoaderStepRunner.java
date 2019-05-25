package org.ananas.runner.kernel;

import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.kernel.model.StepType;

public class LoaderStepRunner extends AbstractStepRunner {

  protected transient Step step;
  protected transient StepRunner previous;
  protected boolean isTest;

  protected LoaderStepRunner(Step step, StepRunner previous, boolean isTest) {
    super(StepType.Loader);

    this.stepId = step.id;
    this.step = step;
    this.previous = previous;
    this.isTest = isTest;
  }
}
