package org.ananas.runner.model.api.pipeline;

import lombok.Data;
import org.ananas.runner.kernel.model.Step;

@Data
public class PipelineStepsResponse {
  public String code;
  public Step[] data;
}
