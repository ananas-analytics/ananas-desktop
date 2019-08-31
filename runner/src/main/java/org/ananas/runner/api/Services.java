package org.ananas.runner.api;

import freemarker.template.TemplateException;
import java.util.HashMap;
import java.util.Map;
import org.ananas.runner.kernel.build.Builder;
import org.ananas.runner.kernel.build.DagBuilder;
import org.ananas.runner.kernel.common.JsonUtil;
import org.ananas.runner.kernel.job.BeamRunner;
import org.ananas.runner.kernel.job.Runner;
import org.ananas.runner.kernel.model.DagRequest;
import org.ananas.runner.kernel.model.Dataframe;

public class Services {

  public static Object runDag(String id, String token, String body)
      throws java.io.IOException, TemplateException {
    DagRequest req = JsonUtil.fromJson(body, DagRequest.class);
    req = req.resolveVariables();

    Builder builder = new DagBuilder(req, false);

    Runner runner = new BeamRunner();
    String jobId = runner.run(builder, id, token, req);
    Map<String, String> map = new HashMap<>();
    map.put("jobid", jobId);
    return JsonUtil.toJson(ApiResponseBuilder.Of().OK(map).build());
  }

  protected static Object testDag(String body) throws java.io.IOException, TemplateException {
    DagRequest req = JsonUtil.fromJson(body, DagRequest.class);
    req = req.resolveVariables();
    return testDag(req);
  }

  public static Object testDag(DagRequest req) {
    Map<String, Dataframe> results = new DagBuilder(req, true).test();
    return JsonUtil.toJson(ApiResponseBuilder.Of().OK(results).build());
  }
}
