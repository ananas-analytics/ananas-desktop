package org.ananas.runner.kernel;

import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.kernel.model.StepType;
import org.apache.beam.sdk.Pipeline;

public class ConnectorStepRunner extends AbstractStepRunner {

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
