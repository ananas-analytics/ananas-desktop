package org.ananas.runner.core.job;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReentrantLock;
import org.ananas.runner.core.StepRunner;
import org.ananas.runner.core.build.Builder;
import org.ananas.runner.core.pipeline.PipelineContext;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.PipelineResult.State;
import org.apache.commons.lang3.tuple.MutablePair;

public class LocalJobManager implements JobManager, JobRepository {

  private ReentrantLock lock;
  private JobRepository jobRepository;

  private static LocalJobManager singleton;

  private LocalJobManager(JobRepository j) {
    this.lock = new ReentrantLock();
    this.jobRepository = j;
  }

  public static LocalJobManager Of(JobRepository j) {
    if (singleton == null) {
      singleton = new LocalJobManager(j);
    }
    return singleton;
  }

  @Override
  public String run(String jobId, Builder builder, String projectId, String token) {
    this.lock.lock();
    Job job =
        Job.of(
            token,
            jobId,
            projectId,
            builder.getEngine(),
            builder.getDag(),
            builder.getGoals(),
            builder.getParams(),
            builder.getTrigger().id);
    this.jobRepository.upsertJob(job);
    try {
      CompletableFuture<MutablePair<PipelineResult, Exception>> pipelineFuture =
          CompletableFuture.supplyAsync(
              () -> {
                try {
                  MutablePair<Map<String, StepRunner>, Stack<PipelineContext>> build =
                      builder.build(jobId);
                  Iterator<PipelineContext> it = build.getRight().iterator();
                  PipelineResult lastIntermediateResult = null;
                  while (it.hasNext()) {
                    if (lastIntermediateResult != null
                        && lastIntermediateResult.getState() != PipelineResult.State.DONE) {
                      return MutablePair.of(lastIntermediateResult, (Exception) null); // We abort
                    }
                    PipelineContext next = it.next();
                    // update the job state
                    job.state = State.RUNNING.name();
                    this.jobRepository.upsertJob(job);
                    if (it.hasNext()) {
                      // Important : we wait for a pipelines to finish before we trigger the next
                      // one.
                      next.waitUntilFinish();
                    } else {
                      lastIntermediateResult = next.run();
                    }
                  }
                  return MutablePair.of(lastIntermediateResult, (Exception) null);
                } catch (Exception e) {
                  System.out.println(e.getMessage());
                  e.printStackTrace();
                  return MutablePair.of((PipelineResult) null, e);
                }
              });

      pipelineFuture.thenApply(
          result -> {
            job.setResult(result);
            // create status polling
            ScheduledExecutorService es = Executors.newSingleThreadScheduledExecutor();
            es.submit(
                () -> {
                  result.getLeft().waitUntilFinish();
                  job.setResult(result);
                  this.jobRepository.upsertJob(job);
                });
            return result != null;
          });

    } finally {
      this.lock.unlock();
    }
    return jobId;
  }

  @Override
  public void cancelJob(String id) throws IOException {
    Job r = this.jobRepository.getJob(id);
    if (r != null) {
      this.lock.lock();
      try {
        r.cancel();
      } finally {
        this.lock.unlock();
      }
    }
  }

  @Override
  public Job upsertJob(Job job) {
    return this.jobRepository.upsertJob(job);
  }

  @Override
  public Job getJob(String id) {
    return this.jobRepository.getJob(id);
  }

  @Override
  public Set<Job> getJobs(int offset, int n) {
    return this.jobRepository.getJobs(offset, n);
  }

  @Override
  public List<Job> getJobsByScheduleId(String scheduleId, int skip, int n) {
    return this.jobRepository.getJobsByScheduleId(scheduleId, skip, n);
  }

  @Override
  public List<Job> getJobsByGoal(String goalId, int skip, int n) {
    return this.jobRepository.getJobsByGoal(goalId, skip, n);
  }

  @Override
  public void deleteJob(String jobId) {
    this.jobRepository.deleteJob(jobId);
  }
}
