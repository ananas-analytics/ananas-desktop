package org.ananas.runner.steprunner.api;

import java.util.HashMap;
import java.util.Map;
import org.ananas.runner.legacy.core.StepConfig;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;

public class APIStepConfig {

  // API
  private static final String API_METHOD = "method";
  private static final String API_FORMAT = "format";
  private static final String API_BODY = "body";
  private static final String API_URL = "url";
  private static final String API_JSONPATH = "jsonpath";
  private static final Object API_DELIM = "lineDelimiter";
  private static final Object API_HEADERS = "headers";

  public Map<String, String> headers;
  public String url;
  public String body;
  public String method;
  public String jsonPath;
  public String format;
  public String delimiter;

  public APIStepConfig(Map<String, Object> config) {
    this.headers = (Map) config.getOrDefault(API_HEADERS, new HashMap<String, String>());
    this.url = (String) config.get(API_URL);
    this.body = (String)config.get(API_BODY);
    this.jsonPath = (String)config.getOrDefault(API_JSONPATH, "");
    this.method = (String) config.get(API_METHOD);
    this.format = (String) config.get(API_FORMAT);
    this.delimiter = (String) config.getOrDefault(API_DELIM, "\n");
    Preconditions.checkNotNull(this.url, API_URL);
    Preconditions.checkNotNull(this.method, API_METHOD);
  }
}
