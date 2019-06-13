package org.ananas.runner.kernel.job;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.IOException;
import java.sql.Date;
import java.util.Set;
import lombok.Data;
import org.ananas.runner.kernel.model.Dag;
import org.ananas.runner.kernel.model.Engine;
import org.ananas.runner.kernel.model.TriggerOptions;
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
  public TriggerOptions trigger;
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
    this.lastUpdate = this.pipelineResult.getState();
    this.state = this.lastUpdate.name();
    this.e = result.getRight();
    if (this.e != null) {
      this.message = this.e.getLocalizedMessage();
    }
  }

  public static Job of(
      String token, String id, String projectId, Engine engine, Dag dag, Set<String> goals, TriggerOptions trigger) {
    long current = System.currentTimeMillis();
    Job s = new Job();
    s.token = token;
    s.id = id;
    s.projectId = projectId;
    s.engine = engine;
    s.goals = goals;
    s.trigger = trigger;
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
      TriggerOptions trigger,
      PipelineResult r,
      Exception e) {
    Job s = Job.of(token, id, projectId, engine, dag, goals, trigger);
    s.setResult(new MutablePair<>(r, e));
    return s;
  }

  public static Job JobStateResultFilter(Job job) {
    Job j = new Job();
    j.id = job.id;
    j.projectId = job.projectId;
    j.state = job.state;
    j.message = job.message;
    j.trigger = job.trigger;
    j.updateAt = job.updateAt;
    j.createAt = job.createAt;
    return j;
  }
}
