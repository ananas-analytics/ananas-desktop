package org.ananas.runner.kernel.job;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.ananas.runner.kernel.build.Builder;

/**
 * TODO: to be implemented
 *
 */
public class JdbcJobManager implements JobManager, JobRepository {
  private static JdbcJobManager INSTANCE = null;

  public JdbcJobManager() {
  }

  public JdbcJobManager of() {
    if (INSTANCE == null) {
      INSTANCE = new JdbcJobManager();
    }
    return INSTANCE;
  }

  @Override
  public String run(String jobId, Builder builder, String projectId, String token) {
    return null;
  }

  @Override
  public void cancelJob(String id) throws IOException {

  }

  @Override
  public void deleteJob(String jobId) {}

  @Override
  public Job getJob(String id) {
    return null;
  }

  @Override
  public Set<Job> getJobs(int offset, int n) {
    return null;
  }

  @Override
  public List<Job> getJobsByScheduleId(String triggerId, int offset, int n) {
    return null;
  }

  @Override
  public List<Job> getJobsByGoal(String goalId, int offset, int n) {
    return null;
  }
}
