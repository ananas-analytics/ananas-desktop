package org.ananas.runner.core.job;

import java.io.IOException;
import java.util.Set;
import org.ananas.runner.core.build.Builder;
import org.ananas.runner.core.model.DagRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeamRunner implements Runner {

  private static final Logger LOG = LoggerFactory.getLogger(BeamRunner.class);

  @Override
  public String run(Builder p, String projectId, String token, DagRequest req) throws IOException {
    JobClient jobApiClient = new LocalJobApiClient();

    String jobId = jobApiClient.createJob(projectId, token, req);

    JobManagerFactory.getJobManager().run(jobId, p, projectId, token);
    return jobId;
  }

  @Override
  public void cancel(String id) throws IOException {
    JobManagerFactory.getJobManager().cancelJob(id);
    LOG.debug("cancelled job id " + id);
  }

  @Override
  public Job getJob(String id) {
    return JobRepositoryFactory.getJobRepostory().getJob(id);
  }

  @Override
  public Set<Job> getJobs() {
    return JobRepositoryFactory.getJobRepostory().getJobs(0, Integer.MAX_VALUE);
  }

  @Override
  public void updateJobState(String jobId) {
    JobClient jobApiClient = new LocalJobApiClient();
    try {
      jobApiClient.updateJobState(jobId);
      LOG.debug("updated job state - " + jobId);
    } catch (IOException e) {
      LOG.warn("Cannot update state of job id : " + jobId, e);
    }
  }

  @Override
  public void removeJob(String jobId) {
    JobRepositoryFactory.getJobRepostory().deleteJob(jobId);
  }
}
