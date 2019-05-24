package org.ananas.runner.model.steps.commons.jobs;

import java.util.Set;
import org.ananas.runner.model.core.Job;

public interface JobRepository {
  Job getJob(String id);

  Set<Job> getJobs();
}
