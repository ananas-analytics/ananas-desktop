package org.ananas.runner.model.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;
import lombok.Data;
import org.ananas.runner.kernel.model.Variable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaginationBody {
  public String type;
  public Map<String, Variable> params;
  public Map<String, Object> config;
}
