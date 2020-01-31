package org.ananas.runner.core.job;

public class JobRepositoryFactory {
  public static JobRepository getJobRepostory() {
    return LocalJobManager.Of(RemoteJobRepository.Of());
  }
}
