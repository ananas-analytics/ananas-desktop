package org.ananas.runner.legacy.core;

public class StepConfig {
  public static final String SHARD = "shard";

  // KEYS
  public static final String SUBTYPE = "subtype";
  public static final String PLATFORM = "platform";
  public static final String PATH = "path";
  public static final String IS_HEADER = "header";
  public static final String DELIMITER = "delimiter";
  public static final String recordSeparator = "recordSeparator";
  public static final String SQL = "sql";
  public static final String FORMAT = "format";
  public static final String PREFIX = "prefix";

  // Mongo
  public static final String MONGO_HOST = "host";
  public static final String MONGO_PORT = "port";
  public static final String DATABASE = "database";
  public static final String MONGO_FILTERS = "filters";
  public static final String COLLECTION = "collection";
  public static final String IS_TEXT = "text";

  // JDBC
  public static final String JDBC_OVERWRITE = "overwrite";
  public static final String JDBC_TYPE = "database";
  public static final String JDBC_URL = "url";
  public static final String JDBC_USER = "user";
  public static final String JDBC_PASSWORD = "password";
  public static final String JDBC_SQL = "sql";
  public static final String JDBC_SQL_DIALECT_TYPE = "inputDialect";
  public static final String JDBC_TABLENAME = "tablename";

  // DATAVIEW
  public static final String DATAVIEW_TABLENAME = "tablename";
  public static final String DATAVIEW_SQL = "sql";

  // JAVASCRIPT
  public static final String JAVASCRIPT_SAMPLE = "sample";
  public static final String JAVASCRIPT_SCRIPT = "script";

  // PIPELINE CONNECT
  public static final String CONNECT_TO_PIPELINEID = "pipelineid";

  // JOIN
  public static final String JOIN_LEFT_STEPID = "leftstepid";
  public static final String JOIN_MAP = "joinedcolumnmap";
  public static final String JOIN_TYPE = "jointype";
  public static final String JOIN_LEFT_COLUMNS = "leftcolumns";
  public static final String JOIN_RIGHT_COLUMNS = "rightcolumns";

  // EXCEL_SHEET_NAME
  public static final String EXCEL_SHEET_NAME = "sheetname";

  // API
  public static final String API_METHOD = "method";
  public static final String API_FORMAT = "format";
  public static final String API_BODY = "body";
  public static final String API_HEADERS = "headers";
  public static final String API_URL = "url";
  public static final String API_JSONPATH = "jsonpath";
  public static final Object API_DELIM = "delimiter";

  // VALUES
  public static final String TYPE_TRANSFORMER = "transformer";
  public static final String TYPE_LOADER = "loader";
  public static final String TYPE_DATAVIEWER = "viewer";

  // ML
  public static final String ML_ALGO = "algo";
  public static final String ML_CLUSTER_NUM = "kcluster"; // Number of cluster
  // classification or regression
  public static final String ML_TREES = "ntrees"; // Classification Decision tree number of tree
  public static final String ML_LAMBDA = "lambda";
  public static final String ML_ALPHA = "alpha";
  public static final String ML_MODE = "mode";
  public static final String ML_MAX_NODES = "maxnodes";
  public static final String ML_NN_UNITS = "units";
  public static final String ML_NN_ACTIVATION_FUNCTION = "activation_fn";
  // Classification Decision  the maximum number of leaf nodes in the trees
  public static final String ML_Y_COLUMN_NAME = "y_colname";
  public static final String ML_SHRINKAGE = "shrinkage";
  public static final String ML_SUBSAMPLE = "subsample";
  public static final String ML_DISTANCE = "distance";

  public static final String TRAIN_ID = "trainId";
  public static final String PREDICT_ID = "predictId";
  public static final String TEST_ID = "testId";
  public static final String ML_K = "knn";
  public static final String ML_FT = "ft";

}
