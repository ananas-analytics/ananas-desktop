package org.ananas.runner.legacy.api.pipeline;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import org.ananas.runner.kernel.common.JsonUtil;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.kernel.pipeline.Pipeline;
import org.ananas.runner.legacy.api.AnanasApiClient;
import org.ananas.runner.misc.HttpClient;

public class PipelineApiClient extends AnanasApiClient implements PipelineClient {

  public PipelineApiClient() {
    super();
  }

  private Pipeline getPipelineInfo(String id, String token) throws IOException {
    String url = String.format("%s/pipelines/%s", this.endpoint, id);
    Map<String, String> params =
        ImmutableMap.<String, String>builder().put("Authorization", token).build();

    Pipeline p = new Pipeline();

    return HttpClient.GET(
        url,
        params,
        conn -> {
          PipelineResponse response =
              JsonUtil.fromJson(conn.getInputStream(), PipelineResponse.class);
          p.id = response.data.id;
          p.projectId = response.data.projectId;
          return p;
        });
  }

  @Override
  public Pipeline getPipeline(String id, String token, boolean getProjectId) throws IOException {
    String url = String.format("%s/pipelines/%s/steps", this.endpoint, id);
    Map<String, String> params =
        ImmutableMap.<String, String>builder().put("Authorization", token).build();

    Pipeline p = true ? getPipelineInfo(id, token) : new Pipeline();
    return HttpClient.GET(
        url,
        params,
        conn -> {
          PipelineStepsResponse response =
              JsonUtil.fromJson(conn.getInputStream(), PipelineStepsResponse.class);
          Step[] steps = response.data;
          p.id = id;
          p.steps = new LinkedList<>();
          for (Step s : steps) {
            p.steps.add(s);
          }
          return p;
        });
  }
}
