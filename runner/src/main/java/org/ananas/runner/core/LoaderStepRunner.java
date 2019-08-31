package org.ananas.runner.core;

import org.ananas.runner.core.model.Step;
import org.ananas.runner.core.model.StepType;

public class LoaderStepRunner extends AbstractStepRunner {

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
