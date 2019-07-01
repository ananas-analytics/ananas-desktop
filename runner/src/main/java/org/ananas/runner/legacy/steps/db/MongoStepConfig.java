package org.ananas.runner.legacy.steps.db;

import java.util.Map;
import org.ananas.runner.legacy.core.StepConfig;

/** Mongo Step Config */
public class MongoStepConfig {

  public String host;
  String port;
  String database;
  String collection;
  String filters;
  boolean isText;

  public MongoStepConfig(Map<String, Object> config) {
    this.host = (String) config.get(StepConfig.MONGO_HOST);
    this.port = (String) config.get(StepConfig.MONGO_PORT);
    this.database = (String) config.get(StepConfig.DATABASE);
    this.collection = (String) config.get(StepConfig.COLLECTION);
    this.filters = (String) config.getOrDefault(StepConfig.MONGO_FILTERS, null);

    this.isText = (boolean) config.getOrDefault(StepConfig.IS_TEXT, false);
  }

  public String getUrl() {
    return "mongodb://" + this.host + ":" + this.port;
  }
}
