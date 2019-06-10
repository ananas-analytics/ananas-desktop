package org.ananas.runner.kernel.model;

import java.io.IOException;
import java.util.Set;
import lombok.Data;
import org.apache.beam.sdk.PipelineResult;
import org.apache.commons.lang3.tuple.MutablePair;

@Data
public class Job {

  public String id;
  public String projectId;
  public Set<String> goals;
  public Engine engine;
  public String token;

  private PipelineResult pipelineResult;
  private Exception e;
  public PipelineResult.State lastUpdate;

  public void cancel() throws IOException {
    this.pipelineResult.cancel();
  }

  public MutablePair<PipelineResult.State, Exception> getState() {
    if (this.pipelineResult == null) {
      return MutablePair.of(PipelineResult.State.FAILED, this.e);
    }
    return MutablePair.of(this.pipelineResult.getState(), this.e);
  }

  public void setResult(MutablePair<PipelineResult, Exception> result) {
    this.pipelineResult = result.getLeft();
    this.e = result.getRight();
  }

  public static Job of(
      String token, String id, String projectId, Engine engine, Set<String> goals) {
    Job s = new Job();
    s.token = token;
    s.id = id;
    s.projectId = projectId;
    s.engine = engine;
    s.goals = goals;
    s.pipelineResult = null;
    s.e = null;
    s.lastUpdate = PipelineResult.State.UNKNOWN;
    return s;
  }

  public static Job of(
      String token,
      String id,
      String projectId,
      Engine engine,
      Set<String> goals,
      PipelineResult r,
      Exception e) {
    Job s = Job.of(token, id, projectId, engine, goals);
    s.setResult(new MutablePair<>(r, e));
    return s;
  }
}
