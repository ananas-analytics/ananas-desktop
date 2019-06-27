package org.ananas.runner.kernel.job;

import java.io.IOException;
import org.ananas.runner.kernel.build.Builder;

public interface JobManager {

  String run(String jobId, Builder builder, String projectId, String token);

  void cancelJob(String id) throws IOException;
}
