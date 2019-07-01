package org.ananas.runner.kernel.job;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import org.ananas.runner.kernel.StepRunner;
import org.ananas.runner.kernel.build.Builder;
import org.ananas.runner.kernel.pipeline.PipelineContext;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.PipelineResult.State;
import org.apache.commons.lang3.tuple.MutablePair;

public class LocalJobManager implements JobManager, JobRepository {

  private ReentrantLock lock;
  private ConcurrentMap<String, Job> jobs;

  private static LocalJobManager singleton;

  private LocalJobManager() {
    this.lock = new ReentrantLock();
    this.jobs = new ConcurrentHashMap<>();
  }

  public static LocalJobManager Of() {
    if (singleton == null) {
      singleton = new LocalJobManager();
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
    this.jobs.put(jobId, job);
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
    Job r = this.jobs.get(id);
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
  public Job getJob(String id) {
    return this.jobs.get(id);
  }

  @Override
  public Set<Job> getJobs(int offset, int n) {
    // ingore offset and n for now, as it is in memory
    Set<Job> jobs = new HashSet<>();
    for (String id : this.jobs.keySet()) {
      Job j = getJob(id);
      if (j != null) {
        jobs.add(getJob(id));
      }
    }
    return jobs;
  }

  @Override
  public List<Job> getJobsByScheduleId(String scheduleId, int skip, int n) {
    return jobs.values().stream()
        .filter(job -> scheduleId.equals(job.scheduleId))
        .sorted((a, b) -> (int) (b.createAt - a.createAt))
        .skip(skip)
        .limit(n)
        .collect(Collectors.toList());
  }

  @Override
  public List<Job> getJobsByGoal(String goalId, int skip, int n) {
    return jobs.values().stream()
        .filter(job -> job.goals != null && job.goals.contains(goalId))
        .sorted((a, b) -> (int) (b.createAt - a.createAt))
        .skip(skip)
        .limit(n)
        .collect(Collectors.toList());
  }

  @Override
  public void deleteJob(String jobId) {
    this.lock.lock();
    try {
      this.jobs.remove(jobId);
    } finally {
      this.lock.unlock();
    }
  }
}
