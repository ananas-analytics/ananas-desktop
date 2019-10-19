package org.ananas.runner.core.job;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import org.ananas.runner.core.model.Dag;
import org.ananas.runner.core.model.Engine;
import org.ananas.runner.core.model.Variable;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.PipelineResult.State;
import org.apache.commons.lang3.tuple.MutablePair;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Job {

  public String token;

  public String id;
  public String projectId;
  public String state;
  public String message;
  public Dag dag;
  public Set<String> goals;
  public Engine engine;
  public Map<String, Variable> params;
  public String scheduleId;
  public long createAt;
  public long updateAt;

  private PipelineResult pipelineResult;
  private Exception e;
  public PipelineResult.State lastUpdate;

  public void cancel() throws IOException {
    this.pipelineResult.cancel();
  }

  @JsonIgnore
  public MutablePair<PipelineResult.State, Exception> getResult() {
    if (this.pipelineResult == null) {
      return MutablePair.of(State.RUNNING, this.e);
    }
    return MutablePair.of(this.pipelineResult.getState(), this.e);
  }

  public void setResult(MutablePair<PipelineResult, Exception> result) {
    this.pipelineResult = result.getLeft();
    if (pipelineResult != null) {
      this.lastUpdate = this.pipelineResult.getState();
    } else {
      this.lastUpdate = State.FAILED;
    }
    this.state = this.lastUpdate.name();

    this.e = result.getRight();
    if (this.e != null) {
      this.message = this.e.getLocalizedMessage();
      this.state = State.FAILED.name();
    }
  }

  public static Job of(
      String token,
      String id,
      String projectId,
      Engine engine,
      Dag dag,
      Set<String> goals,
      Map<String, Variable> params,
      String scheduleId) {
    long current = System.currentTimeMillis();
    Job s = new Job();
    s.token = token;
    s.id = id;
    s.projectId = projectId;
    s.engine = engine;
    s.goals = goals;
    s.params = params;
    s.scheduleId = scheduleId;
    s.pipelineResult = null;
    s.e = null;
    s.state = State.UNKNOWN.name();
    s.message = null;
    s.createAt = current;
    s.updateAt = current;

    s.lastUpdate = State.UNKNOWN;
    return s;
  }

  public static Job of(
      String token,
      String id,
      String projectId,
      Engine engine,
      Dag dag,
      Set<String> goals,
      Map<String, Variable> params,
      String scheduleId,
      PipelineResult r,
      Exception e) {
    Job s = Job.of(token, id, projectId, engine, dag, goals, params, scheduleId);
    s.setResult(new MutablePair<>(r, e));
    return s;
  }

  public static Job JobStateResultFilter(Job job) {
    Job j = new Job();
    String state = job.state;
    if (job.pipelineResult != null) {
      state = job.pipelineResult.getState().name();
    }

    j.id = job.id;
    j.projectId = job.projectId;
    j.state = state;
    j.message = job.message;
    j.scheduleId = job.scheduleId;
    j.updateAt = job.updateAt;
    j.createAt = job.createAt;
    return j;
  }
}
