package org.ananas.runner.kernel.job;

import java.io.IOException;
import java.util.Set;
import org.ananas.runner.kernel.build.Builder;
import org.ananas.runner.kernel.model.DagRequest;
import org.ananas.runner.kernel.model.Job;

public interface Runner {

  Set<Job> getJobs();

  /**
   * Runs a pipelines
   *
   * @param p
   * @return Job session Id and JobId
   */
  String run(Builder p, String projectId, String token, DagRequest req) throws IOException;

  void cancel(String id) throws IOException;

  /**
   * Pipeline get job state
   *
   * @return
   */
  Job getJob(String id);

  /**
   * Update Api Job state
   *
   * @param jobId job ID
   */
  void updateJobState(String jobId);

  /**
   * Remove Job
   *
   * @param jobId job ID
   */
  void removeJob(String jobId);
}
