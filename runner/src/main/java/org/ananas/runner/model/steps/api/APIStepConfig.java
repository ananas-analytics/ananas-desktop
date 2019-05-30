package org.ananas.runner.model.steps.api;

import java.util.HashMap;
import java.util.Map;
import org.ananas.runner.model.core.StepConfig;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;

public class APIStepConfig {

  public Map<String, String> headers;
  public String url;
  public Object body;
  public String method;

  public APIStepConfig(Map<String, Object> config) {
    this.headers = (Map) config.getOrDefault(StepConfig.API_HEADERS, new HashMap<String, String>());
    this.url = (String) config.get(StepConfig.API_URL);
    this.body = config.get(StepConfig.API_BODY);
    this.method = (String) config.get(StepConfig.API_METHOD);
    Preconditions.checkNotNull(this.url, StepConfig.API_URL);
    Preconditions.checkNotNull(this.method, StepConfig.API_METHOD);
  }
}
