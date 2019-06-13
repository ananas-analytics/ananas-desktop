package org.ananas.runner.kernel.job;

public class JobRepositoryFactory {
  public static JobRepository getJobRepostory() {
    return LocalJobManager.Of();
  }
}
