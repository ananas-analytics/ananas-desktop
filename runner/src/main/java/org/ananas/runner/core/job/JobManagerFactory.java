package org.ananas.runner.core.job;

public class JobManagerFactory {
  public static JobManager getJobManager() {
    return LocalJobManager.Of();
  }
}
