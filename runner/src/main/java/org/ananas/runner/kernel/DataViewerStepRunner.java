package org.ananas.runner.kernel;

import org.ananas.runner.kernel.model.Engine;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.kernel.model.StepType;

public class DataViewerStepRunner extends AbstractStepRunner {

  protected String jobId;
  protected Engine engine;
  protected transient Step step;
  protected transient StepRunner previous;
  protected boolean isTest;

  public DataViewerStepRunner(Step step, StepRunner previous, Engine engine, String jobId, boolean isTest) {
    super(StepType.Viewer);

    this.stepId = step.id;

    this.jobId = jobId;
    this.step = step;
    this.previous = previous;
    this.engine = engine;
    this.isTest = isTest;
  }
}
