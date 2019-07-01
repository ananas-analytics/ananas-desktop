package org.ananas.runner.kernel.paginate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;
import lombok.Data;
import org.ananas.runner.kernel.model.Dataframe;
import org.ananas.runner.kernel.model.Variable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaginationBody {
  public String type;
  public String metadataId;
  public Map<String, Object> config;
  public Map<String, Variable> params;
  public Dataframe dataframe;
}
