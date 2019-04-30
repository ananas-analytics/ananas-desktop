package org.ananas.runner.model.steps.commons.jobs;

import org.ananas.runner.model.steps.commons.build.Builder;

import java.io.IOException;

public interface JobManager {

	void cancelJob(String id) throws IOException;

	String run(String jobId,
			   Builder builder,
			   String projectId,
			   String token);

	void removeJob(String jobId);

}
