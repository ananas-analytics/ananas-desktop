package org.ananas.runner.model.core;


public class StepConfig {
	public final static String SHARD = "shard";

	//KEYS
	public final static String SUBTYPE = "subtype";
	public final static String PLATFORM = "platform";
	public final static String PATH = "path";
	public final static String IS_HEADER = "header";
	public final static String DELIMITER = "delimiter";
	public final static String recordSeparator = "recordSeparator";
	public final static String SQL = "sql";
	public final static String FORMAT = "format";
	public final static String PREFIX = "prefix";

	//Mongo
	public final static String MONGO_HOST = "host";
	public final static String MONGO_PORT = "port";
	public final static String DATABASE = "database";
	public final static String MONGO_FILTERS = "filters";
	public final static String COLLECTION = "collection";
	public final static String IS_TEXT = "text";

	//JDBC
	public final static String JDBC_OVERWRITE = "overwrite";
	public final static String JDBC_TYPE = "database";
	public final static String JDBC_URL = "url";
	public final static String JDBC_USER = "user";
	public final static String JDBC_PASSWORD = "password";
	public final static String JDBC_SQL = "sql";
	public final static String JDBC_SQL_DIALECT_TYPE = "inputDialect";
	public final static String JDBC_TABLENAME = "tablename";

	//DATAVIEW
	public final static String DATAVIEW_TABLENAME = "tablename";
	public final static String DATAVIEW_SQL = "sql";

	//JAVASCRIPT
	public final static String JAVASCRIPT_SAMPLE = "sample";
	public final static String JAVASCRIPT_SCRIPT = "script";

	//PIPELINE CONNECT
	public static final String CONNECT_TO_PIPELINEID = "pipelineid";

	//JOIN
	public static final String JOIN_LEFT_STEPID = "leftstepid";
	public static final String JOIN_MAP = "joinedcolumnmap";
	public static final String JOIN_TYPE = "jointype";
	public static final String JOIN_LEFT_COLUMNS = "leftcolumns";
	public static final String JOIN_RIGHT_COLUMNS = "rightcolumns";

	//EXCEL_SHEET_NAME
	public static final String EXCEL_SHEET_NAME = "sheetname";

	//API
	public static final String API_METHOD = "method";
	public static final String API_BODY = "body";
	public static final String API_HEADERS = "headers";
	public static final String API_URL = "url";


	//VALUES
	public static final String TYPE_TRANSFORMER = "transformer";
	public static final String TYPE_LOADER = "loader";
	public static final String TYPE_DATAVIEWER = "viewer";


	//ML
	public static final String ML_ALGO = "algo";
	public static final String ML_CLUSTER_NUM = "kcluster"; //Number of cluster
	//classification or regression
	public static final String ML_TREES = "ntrees"; //Classification Decision tree number of tree
	public static final String ML_LAMBDA = "lambda";
	public static final String ML_ALPHA = "alpha";
	public static final String ML_MODE = "mode";
	public static final String ML_MAX_NODES = "maxnodes";
	public static final String ML_NN_UNITS = "units";
	public static final String ML_NN_ACTIVATION_FUNCTION = "activation_fn";
	//Classification Decision  the maximum number of leaf nodes in the trees
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
