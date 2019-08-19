package org.ananas.runner.steprunner.mongo;

import com.google.common.base.Preconditions;
import java.util.Map;

/** Mongo Step Config */
public class MongoStepConfig {

  // Mongo
  private static final String MONGO_HOST = "host";
  private static final String MONGO_PORT = "port";
  private static final String DATABASE = "database";
  private static final String MONGO_FILTERS = "filters";
  private static final String COLLECTION = "collection";
  private static final String IS_TEXT = "json";

  public String host;
  String port;
  String database;
  String collection;
  String filters;
  boolean isText;

  public MongoStepConfig(Map<String, Object> config) {
    this.host = (String) config.get(MONGO_HOST);
    this.port = (String) config.get(MONGO_PORT);
    this.database = (String) config.get(DATABASE);
    this.collection = (String) config.get(COLLECTION);
    this.filters = (String) config.getOrDefault(MONGO_FILTERS, null);

    this.isText = (boolean) config.getOrDefault(IS_TEXT, false);
    Preconditions.checkNotNull(this.host, "Host cannot be empty");
    Preconditions.checkNotNull(this.port, "port cannot be empty");
    Preconditions.checkNotNull(this.database, "database name cannot be empty");
    Preconditions.checkNotNull(this.collection, "collection name cannot be empty");
  }

  public String getUrl() {
    return "mongodb://" + this.host + ":" + this.port;
  }
}
