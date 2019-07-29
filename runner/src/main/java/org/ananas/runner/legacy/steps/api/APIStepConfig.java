package org.ananas.runner.legacy.steps.api;

import java.util.HashMap;
import java.util.Map;
import org.ananas.runner.legacy.core.StepConfig;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;

public class APIStepConfig {

  public Map<String, String> headers;
  public String url;
  public String body;
  public String method;
  public String jsonPath;
  public String format;
  public String delimiter;

  public APIStepConfig(Map<String, Object> config) {
    this.headers = new HashMap<>(); //(Map) config.getOrDefault(StepConfig.API_HEADERS, new HashMap<String, String>());
    this.url = (String) config.get(StepConfig.API_URL);
    this.body = (String)config.get(StepConfig.API_BODY);
    this.jsonPath = (String)config.getOrDefault(StepConfig.API_JSONPATH, "");
    this.method = (String) config.get(StepConfig.API_METHOD);
    this.format = (String) config.get(StepConfig.API_FORMAT);
    this.delimiter = (String) config.getOrDefault(StepConfig.API_DELIM, "\n");
    Preconditions.checkNotNull(this.url, StepConfig.API_URL);
    Preconditions.checkNotNull(this.method, StepConfig.API_METHOD);
  }
}
