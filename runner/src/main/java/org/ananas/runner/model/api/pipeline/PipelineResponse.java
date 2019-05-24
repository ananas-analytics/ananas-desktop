package org.ananas.runner.model.api.pipeline;

import lombok.Data;

@Data
public class PipelineResponse {
  public String code;
  public PipelineInfo data;
}
