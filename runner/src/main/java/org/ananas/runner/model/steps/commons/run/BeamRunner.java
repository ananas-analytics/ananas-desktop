package org.ananas.runner.model.steps.commons.run;

import java.io.IOException;
import java.util.Set;
import org.ananas.runner.kernel.build.Builder;
import org.ananas.runner.kernel.model.DagRequest;
import org.ananas.runner.model.api.job.JobClient;
import org.ananas.runner.model.api.job.LocalJobApiClient;
import org.ananas.runner.model.core.Job;
import org.ananas.runner.model.steps.commons.jobs.LocalJobManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeamRunner implements Runner {

  private static final Logger LOG = LoggerFactory.getLogger(BeamRunner.class);

  @Override
  public String run(Builder p, String projectId, String token, DagRequest req) throws IOException {
    JobClient jobApiClient = new LocalJobApiClient();

    String jobId = jobApiClient.createJob(projectId, token, req);

    LocalJobManager.Of().run(jobId, p, projectId, token);
    return jobId;
  }

  @Override
  public void cancel(String id) throws IOException {
    LocalJobManager.Of().cancelJob(id);
    LOG.debug("cancelled job id " + id);
  }

  @Override
  public Job getJob(String id) {
    return LocalJobManager.Of().getJob(id);
  }

  @Override
  public Set<Job> getJobs() {
    return LocalJobManager.Of().getJobs();
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
    LocalJobManager.Of().removeJob(jobId);
  }
}
