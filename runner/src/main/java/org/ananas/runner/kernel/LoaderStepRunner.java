package org.ananas.runner.kernel;

import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.kernel.model.StepType;

public abstract class LoaderStepRunner extends AbstractStepRunner {

  private static final long serialVersionUID = -1873968600892797483L;

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
