package org.ananas.runner.legacy.api.pipeline;

import lombok.Data;

@Data
public class PipelineResponse {
  public String code;
  public PipelineInfo data;
}
