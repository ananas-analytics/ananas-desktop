package org.ananas.runner.core.job;

import java.util.List;
import java.util.Set;

public interface JobRepository {

  /**
   * AUpdate or insert a job
   *
   * @param job
   * @return the added job
   */
  Job upsertJob(Job job);

  /**
   * Get job by id
   *
   * @param id
   * @return
   */
  Job getJob(String id);

  /**
   * Get all jobs
   *
   * @return
   */
  Set<Job> getJobs(int offset, int n);

  /**
   * Get jobs by trigger id, for scheduled job
   *
   * @param triggerId
   * @return
   */
  List<Job> getJobsByScheduleId(String triggerId, int offset, int n);

  /**
   * Get jobs by goal
   *
   * @param goalId
   * @return
   */
  List<Job> getJobsByGoal(String goalId, int offset, int n);

  /**
   * Delete a job
   *
   * @param jobId
   */
  void deleteJob(String jobId);
}
