package org.ananas.runner.model.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaginationBody {
  public String type;
  public Map<String, DagRequest.Variable> params;
  public Map<String, Object> config;
}
