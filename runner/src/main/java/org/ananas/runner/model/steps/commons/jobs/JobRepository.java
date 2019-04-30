package org.ananas.runner.model.steps.commons.jobs;

import org.ananas.runner.model.core.Job;

import java.util.Set;

public interface JobRepository {
	Job getJob(String id);

	Set<Job> getJobs();

}
