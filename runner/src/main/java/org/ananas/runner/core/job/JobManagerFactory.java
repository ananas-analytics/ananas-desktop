package org.ananas.runner.core.job;

public class JobManagerFactory {

  public static JobManager getJobManager() {
    return getJobManager(true); // TODO set it as setting parameter
  }

  public static JobManager getJobManager(boolean isLocal) {
    return LocalJobManager.Of(isLocal ? LocalJobRepository.Of() : RemoteJobRepository.Of());
  }
}
