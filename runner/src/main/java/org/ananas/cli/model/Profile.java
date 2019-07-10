package org.ananas.cli.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.ananas.runner.kernel.model.Engine;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Profile {
  public Engine engine;
  public Map<String, String> params;

  public Profile() {
    params = new HashMap<>();
  }
}
