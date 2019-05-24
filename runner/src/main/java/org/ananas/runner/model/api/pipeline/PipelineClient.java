package org.ananas.runner.model.api.pipeline;

import java.io.IOException;
import org.ananas.runner.model.core.Pipeline;

public interface PipelineClient {

  Pipeline getPipeline(String id, String token, boolean getProjectId) throws IOException;
}
