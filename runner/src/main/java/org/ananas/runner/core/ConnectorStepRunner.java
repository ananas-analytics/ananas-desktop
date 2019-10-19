package org.ananas.runner.core;

import org.ananas.runner.core.model.Step;
import org.ananas.runner.core.model.StepType;
import org.apache.beam.sdk.Pipeline;

public abstract class ConnectorStepRunner extends AbstractStepRunner {

  private static final long serialVersionUID = 6428329144693419800L;
  protected transient Pipeline pipeline;
  protected transient Step step;
  protected boolean doSampling;
  protected boolean isTest;

  public ConnectorStepRunner(Pipeline pipeline, Step step, boolean doSampling, boolean isTest) {
    super(StepType.Connector);

    // for AbstractStepRunner
    this.stepId = step.id;

    this.pipeline = pipeline;
    this.step = step;
    this.doSampling = doSampling;
    this.isTest = isTest;
  }
}
