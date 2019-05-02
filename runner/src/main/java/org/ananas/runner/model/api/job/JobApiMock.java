package org.ananas.runner.model.api.job;

import com.google.common.collect.ImmutableMap;
import org.ananas.runner.model.api.AnanasApiClient;
import org.ananas.runner.model.core.DagRequest;
import org.ananas.runner.model.core.Job;
import org.ananas.runner.model.steps.commons.jobs.LocalJobManager;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

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
		//curl -sX PUT $ENDPOINT/job/$JOB_ID/state \
		//-H "Content-Type: application/json" \
		//-H "Authorization: $TOKEN" \
		//-d "{\"state\": \"done\"}" \


		Job job = LocalJobManager.Of().getJob(jobId);

		if (job != null && job.getState().getLeft() != job.lastUpdate) {
			job.lastUpdate = job.getState().getLeft();
			String url =
					String.format("%s/job/%s/state", this.endpoint, jobId);

			Map<String, Object> body = ImmutableMap.of("state", job.getState().getLeft().name());

			System.out.println(body);
		}
	}

}
