package org.ananas.runner.core.job;

import java.io.IOException;
import org.ananas.runner.core.build.Builder;

public interface JobManager {

  String run(String jobId, Builder builder, String projectId, String token);

  void cancelJob(String id) throws IOException;
}
