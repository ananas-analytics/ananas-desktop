package org.ananas.runner.kernel.job;

public class JobManagerFactory {
  public static JobManager getJobManager() {
    return LocalJobManager.Of();
  }
}
