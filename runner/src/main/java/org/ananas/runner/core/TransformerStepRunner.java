package org.ananas.runner.core;

import org.ananas.runner.core.model.Step;
import org.ananas.runner.core.model.StepType;

public abstract class TransformerStepRunner extends AbstractStepRunner {

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
