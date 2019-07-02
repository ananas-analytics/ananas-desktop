package org.ananas.runner.steprunner.gcs;

import java.util.Map;
import org.ananas.runner.misc.StepConfigHelper;

public class BigQueryStepConfig {
  public static final String PROJECT = "project";
  public static final String DATASET = "dataset";
  public static final String TABLENAME = "tablename";
  public static final String QUERY = "sql";

  public String projectId;
  public String dataset;
  public String tablename;
  public String sql;

  public BigQueryStepConfig(Map<String, Object> config) {
    projectId = StepConfigHelper.getConfig(config, PROJECT, "");
    dataset = StepConfigHelper.getConfig(config, DATASET, "");
    tablename = StepConfigHelper.getConfig(config, TABLENAME, "");
    sql = StepConfigHelper.getConfig(config, QUERY, "");
  }

  public String getCompleteTableName() {
    return projectId + "." + dataset + "." + tablename;
  }

  public String getQuery() {
    return this.sql.replace("[TABLE]", "`" + getCompleteTableName() + "`");
  }
}
