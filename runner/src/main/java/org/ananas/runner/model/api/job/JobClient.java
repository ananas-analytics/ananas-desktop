package org.ananas.runner.model.api.job;

import org.ananas.runner.model.core.DagRequest;

import java.io.IOException;

public interface JobClient {

	String createJob(String projectId, String token, DagRequest dagRequest) throws IOException;

	void updateJobState(String jobId) throws IOException;
}
