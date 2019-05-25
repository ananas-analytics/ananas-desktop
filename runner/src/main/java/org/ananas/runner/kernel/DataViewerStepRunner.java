package org.ananas.runner.kernel;

import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.kernel.model.StepType;

public class DataViewerStepRunner extends AbstractStepRunner {

  protected transient Step step;
  protected transient StepRunner previous;
  protected boolean isTest;

  public DataViewerStepRunner(Step step, StepRunner previous, boolean isTest) {
    super(StepType.Viewer);

    this.stepId = step.id;

    this.step = step;
    this.previous = previous;
    this.isTest = isTest;
  }
}
