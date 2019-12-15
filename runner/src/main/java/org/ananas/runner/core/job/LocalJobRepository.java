package org.ananas.runner.core.job;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class LocalJobRepository implements JobRepository {

  private ReentrantLock lock;
  private ConcurrentMap<String, Job> jobs;

  private static LocalJobRepository singleton;

  private LocalJobRepository() {
    this.lock = new ReentrantLock();
    this.jobs = new ConcurrentHashMap<>();
  }

  public static LocalJobRepository Of() {
    if (singleton == null) {
      singleton = new LocalJobRepository();
    }
    return singleton;
  }

  @Override
  public Job upsertJob(Job job) {
    this.jobs.put(job.id, job);
    return null;
  }

  @Override
  public Job getJob(String id) {
    return this.jobs.get(id);
  }

  @Override
  public Set<Job> getJobs(int offset, int n) {
    // ignore offset and n for now, as it is in memory
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
