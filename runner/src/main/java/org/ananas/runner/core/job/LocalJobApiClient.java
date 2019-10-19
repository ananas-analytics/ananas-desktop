package org.ananas.runner.core.job;

import java.io.IOException;
import java.util.UUID;
import org.ananas.runner.core.model.DagRequest;

public class LocalJobApiClient implements JobClient {

  @Override
  public String createJob(String projectId, String token, DagRequest dagRequest)
      throws IOException {
    // simply create a uuid as job id
    return UUID.randomUUID().toString();
  }

  @Override
  public void updateJobState(String jobId) throws IOException {
    // do't need to implement this method at all
  }
}
