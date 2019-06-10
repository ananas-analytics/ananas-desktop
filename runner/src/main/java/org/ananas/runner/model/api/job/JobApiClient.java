package org.ananas.runner.model.api.job;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.ananas.runner.kernel.common.JsonUtil;
import org.ananas.runner.kernel.errors.ExceptionHandler;
import org.ananas.runner.kernel.job.JobClient;
import org.ananas.runner.kernel.job.LocalJobManager;
import org.ananas.runner.kernel.model.DagRequest;
import org.ananas.runner.kernel.model.Job;
import org.ananas.runner.misc.HttpClient;
import org.ananas.runner.model.api.AnanasApiClient;
import org.ananas.runner.model.api.pipeline.SimpleMapResponse;
import org.apache.commons.io.IOUtils;

public class JobApiClient extends AnanasApiClient implements JobClient {

  public JobApiClient() {
    super();
  }

  @Override
  public String createJob(String projectId, String token, DagRequest dagRequest)
      throws IOException {
    // -d "{ \
    //        \"dag\": {}, \
    //        \"goals\": [\"goal1\"], \
    //        \"env\": { \"name\": \"example env\", \"type\": \"local\" }, \
    //        \"params\": {} \
    // }" \

    String url = String.format("%s/job", this.endpoint, projectId);
    Map<String, String> params =
        ImmutableMap.<String, String>builder().put("Authorization", token).build();

    return HttpClient.POST(
        url,
        params,
        dagRequest,
        conn -> {
          String s = IOUtils.toString(conn.getInputStream());
          SimpleMapResponse response = JsonUtil.fromJson(s, SimpleMapResponse.class);
          return response.data.id;
        });
  }

  @Override
  public void updateJobState(String jobId) throws IOException {
    // curl -sX PUT $ENDPOINT/job/$JOB_ID/state \
    // -H "Content-Type: application/json" \
    // -H "Authorization: $TOKEN" \
    // -d "{\"state\": \"done\"}" \

    Job job = LocalJobManager.Of().getJob(jobId);
    if (job != null && job.getState() != null && job.getState().getLeft() != null) {
      System.out.println(job.getState().toString());
    }
    if (job != null && job.getState() != null && job.getState().getLeft() != job.lastUpdate) {
      job.lastUpdate = job.getState().getLeft();
      String url = String.format("%s/job/%s/state", this.endpoint, jobId);
      Map<String, String> params =
          ImmutableMap.<String, String>builder().put("Authorization", job.token).build();

      Map<String, Object> body = new HashMap<>();
      body.put("state", job.getState().getLeft().name());
      if (job.getState().getRight() != null) {
        body.put("message", ExceptionHandler.valueOf(job.getState().getRight()).error.getRight());
      }

      HttpClient.PUT(
          url,
          params,
          body,
          conn -> {
            return null;
          });
    }
  }
}
