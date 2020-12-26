package org.ananas.runner.steprunner.api;

import java.util.HashMap;
import java.util.Map;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;

public class APIStepConfig {

  // API
  protected static final String API_METHOD = "method";
  protected static final String API_FORMAT = "format";
  protected static final String API_BODY = "body";
  protected static final String API_URL = "url";
  protected static final String API_JSONPATH = "jsonpath";
  protected static final Object API_DELIM = "lineDelimiter";
  protected static final Object API_HEADERS = "headers";

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
    this.body = (String) config.get(API_BODY);
    this.jsonPath = (String) config.getOrDefault(API_JSONPATH, "");
    this.method = (String) config.get(API_METHOD);
    this.format = (String) config.get(API_FORMAT);
    this.delimiter = (String) config.getOrDefault(API_DELIM, "\n");

    Preconditions.checkNotNull(this.url, API_URL);
    Preconditions.checkNotNull(this.method, API_METHOD);
  }

  protected APIStepConfig() {}
}
