package org.ananas.runner.legacy.api.job;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.ananas.runner.kernel.job.Job;
import org.ananas.runner.kernel.job.JobClient;
import org.ananas.runner.kernel.job.LocalJobManager;
import org.ananas.runner.kernel.model.DagRequest;
import org.ananas.runner.legacy.api.AnanasApiClient;

public class JobApiMock extends AnanasApiClient implements JobClient {

  public JobApiMock() {
    super();
  }

  @Override
  public String createJob(String projectId, String token, DagRequest request) throws IOException {
    return UUID.randomUUID().toString();
  }

  @Override
  public void updateJobState(String jobId) throws IOException {
    // curl -sX PUT $ENDPOINT/job/$JOB_ID/state \
    // -H "Content-Type: application/json" \
    // -H "Authorization: $TOKEN" \
    // -d "{\"state\": \"done\"}" \

    Job job = LocalJobManager.Of().getJob(jobId);

    if (job != null && job.getResult().getLeft() != job.lastUpdate) {
      job.lastUpdate = job.getResult().getLeft();
      String url = String.format("%s/job/%s/state", this.endpoint, jobId);

      Map<String, Object> body = ImmutableMap.of("state", job.getResult().getLeft().name());

      System.out.println(body);
    }
  }
}
