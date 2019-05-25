package org.ananas.runner.kernel.pipeline;

import org.apache.beam.sdk.PipelineResult;

public class PipelineContext implements Comparable<PipelineContext> {

  long timeMillis;
  PipelineHook hook;
  org.apache.beam.sdk.Pipeline pipeline;

  private PipelineContext(PipelineHook hook, org.apache.beam.sdk.Pipeline pipeline) {
    this.hook = hook;
    this.pipeline = pipeline;
    this.timeMillis = System.currentTimeMillis();
  }

  public boolean hasHook() {
    return this.getHook() != null && !(this.getHook() instanceof NoHook);
  }

  public static PipelineContext of(PipelineHook hook, org.apache.beam.sdk.Pipeline pipeline) {
    return new PipelineContext(hook, pipeline);
  }

  public PipelineHook getHook() {
    return this.hook;
  }

  public org.apache.beam.sdk.Pipeline getPipeline() {
    return this.pipeline;
  }

  public void setHook(PipelineHook hook) {
    this.hook = hook;
  }

  public PipelineResult.State waitUntilFinish() {
    this.hook.run();
    return this.pipeline.run().waitUntilFinish();
  }

  public PipelineResult run() {
    this.hook.run();
    return this.pipeline.run();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PipelineContext)) {
      return false;
    }

    PipelineContext that = (PipelineContext) o;

    if (getHook() != null ? !getHook().equals(that.getHook()) : that.getHook() != null) {
      return false;
    }
    return getPipeline() != null
        ? getPipeline().equals(that.getPipeline())
        : that.getPipeline() == null;
  }

  @Override
  public int hashCode() {
    int result = getHook() != null ? getHook().hashCode() : 0;
    result = 31 * result + (getPipeline() != null ? getPipeline().hashCode() : 0);
    return result;
  }

  @Override
  public int compareTo(PipelineContext o) {
    return Long.valueOf(this.timeMillis - o.timeMillis).intValue();
  }
}
