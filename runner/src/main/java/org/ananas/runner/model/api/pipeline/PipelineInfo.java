package org.ananas.runner.model.api.pipeline;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.LinkedList;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PipelineInfo {
  public String id;
  public String projectId;
  public String name;
  public String description;
  public LinkedList<String> steps;
}
