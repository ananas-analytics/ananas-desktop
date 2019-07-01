package org.ananas.runner.kernel;

import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.kernel.model.StepType;

public class TransformerStepRunner extends AbstractStepRunner {

  protected transient Step step;
  protected transient StepRunner previous;

  protected TransformerStepRunner(Step step, StepRunner previous) {
    super(StepType.Transformer);

    // for AbstractStepRunner
    this.stepId = step.id;

    this.step = step;
    this.previous = previous;
  }
}
