package org.ananas.runner.model.api.job;

import com.google.common.collect.ImmutableMap;
import org.ananas.runner.api.JsonUtil;
import org.ananas.runner.misc.HttpClient;
import org.ananas.runner.model.api.DatumaniaApiClient;
import org.ananas.runner.model.api.pipeline.SimpleMapResponse;
import org.ananas.runner.model.core.DagRequest;
import org.ananas.runner.model.core.Job;
import org.ananas.runner.model.errors.ExceptionHandler;
import org.ananas.runner.model.steps.commons.jobs.LocalJobManager;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JobApiClient extends DatumaniaApiClient implements JobClient {


	public JobApiClient() {
		super();
	}


	@Override
	public String createJob(String projectId, String token, DagRequest dagRequest) throws IOException {
		//-d "{ \
		//        \"dag\": {}, \
		//        \"goals\": [\"goal1\"], \
		//        \"env\": { \"name\": \"example env\", \"type\": \"local\" }, \
		//        \"params\": {} \
		//}" \

		String url = String.format("%s/job", this.endpoint, projectId);
		Map<String, String> params = ImmutableMap.<String, String>builder().put("Authorization", token).build();


		return HttpClient.POST(url, params, dagRequest, conn -> {
			String s = IOUtils.toString(conn.getInputStream());
			System.out.println(s);
			SimpleMapResponse response = JsonUtil.fromJson(s, SimpleMapResponse.class);
			return response.data.id;
		});
	}


	@Override
	public void updateJobState(String jobId) throws IOException {
		//curl -sX PUT $ENDPOINT/job/$JOB_ID/state \
		//-H "Content-Type: application/json" \
		//-H "Authorization: $TOKEN" \
		//-d "{\"state\": \"done\"}" \


		Job job = LocalJobManager.Of().getJob(jobId);

		if (job != null && job.getState() != null && job.getState().getLeft() != job.lastUpdate) {
			job.lastUpdate = job.getState().getLeft();
			String url =
					String.format("%s/job/%s/state", this.endpoint, jobId);
			Map<String, String> params = ImmutableMap.<String, String>builder().put("Authorization", job.token).build();

			Map<String, Object> body = new HashMap<>();
			body.put("state", job.getState().getLeft().name());
			if (job.getState().getRight() != null) {
				body.put("message", ExceptionHandler.valueOf(
						job.getState().getRight()).error.getRight());
			}


			HttpClient.PUT(url, params, body, conn -> {
				return null;
			});
		}
	}

}
