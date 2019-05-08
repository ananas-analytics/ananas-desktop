package org.ananas.runner.model.steps.commons.jobs;

import org.ananas.runner.model.core.Job;
import org.ananas.runner.model.steps.commons.build.Builder;

import java.io.IOException;
import java.util.List;

public interface JobManager {

	void cancelJob(String id) throws IOException;

	String run(String jobId,
			   Builder builder,
			   String projectId,
			   String token);

	void removeJob(String jobId);
}
