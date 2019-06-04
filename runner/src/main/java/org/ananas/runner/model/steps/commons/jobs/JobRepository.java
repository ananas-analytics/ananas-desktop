package org.ananas.runner.model.steps.commons.jobs;

import java.util.Set;
import org.ananas.runner.kernel.model.Job;

public interface JobRepository {
  Job getJob(String id);

  Set<Job> getJobs();
}
