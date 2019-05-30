package org.ananas.runner.model.api.model;

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
  private PipelineResult pipelineResult;
  private Exception e;
  public PipelineResult.State lastUpdate;
  public String token;

  public void cancel() throws IOException {
    this.pipelineResult.cancel();
  }

  public MutablePair<PipelineResult.State, Exception> getState() {
    if (this.pipelineResult == null) {
      return MutablePair.of(PipelineResult.State.FAILED, this.e);
    }
    return MutablePair.of(this.pipelineResult.getState(), this.e);
  }

  public static Job of(
      String id, PipelineResult r, Exception e, String projectId, Set<String> goals, String token) {
    Job s = new Job();
    s.id = id;
    s.pipelineResult = r;
    s.projectId = projectId;
    s.goals = goals;
    s.token = token;
    s.e = e;
    s.lastUpdate = PipelineResult.State.UNKNOWN;
    return s;
  }
}
