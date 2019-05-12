package org.ananas.runner.api;

import org.ananas.runner.model.core.DagRequest;
import org.ananas.runner.model.core.Dataframe;
import org.ananas.runner.model.steps.commons.build.Builder;
import org.ananas.runner.model.steps.commons.build.DagBuilder;
import org.ananas.runner.model.steps.commons.run.BeamRunner;
import org.ananas.runner.model.steps.commons.run.Runner;

import java.util.HashMap;
import java.util.Map;

public class Services {

	protected static Object runDag(String id, String token, String body) throws java.io.IOException {
		DagRequest req = JsonUtil.fromJson(body, DagRequest.class);

		Builder builder = new DagBuilder(req.dag, false, req.goals, req.params, req.engine);

		Runner runner = new BeamRunner();
		String jobId = runner.run(builder, id, token, req);
		Map<String, String> map = new HashMap<>();
		map.put("jobid", jobId);
		return JsonUtil.toJson(ApiResponseBuilder.Of().OK(map).build());
	}


	protected static Object testDag(DagRequest req) throws java.io.IOException {
		Map<String, Dataframe> results = new DagBuilder(req.dag, true, req.goals, req.params, req.engine).test();
		return JsonUtil.toJson(ApiResponseBuilder.Of().OK(results).build());
	}

	protected static Object testDag(String body) throws java.io.IOException {
		DagRequest req = JsonUtil.fromJson(body, DagRequest.class);
		return testDag(req);
	}


}
