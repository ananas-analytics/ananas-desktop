package org.ananas.runner.paginator.files;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.ananas.runner.kernel.model.StepType;
import org.ananas.runner.kernel.paginate.AutoDetectedSchemaPaginator;
import org.ananas.runner.misc.StepConfigHelper;
import org.ananas.runner.steprunner.gcs.BigQueryHelper;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;

public class BigqueryPaginator extends AutoDetectedSchemaPaginator {

  public static final String PROJECT = "project";
  public static final String DATASET = "dataset";
  public static final String TABLENAME = "tablename";
  public static final String QUERY = "sql";

  public static final String TABLE_PLACEHOLDER = "[TABLE]";

  private String projectId;
  private String dataset;
  private String tablename;
  private String sql;

  public BigqueryPaginator(String id, String type, Map<String, Object> config, Schema schema) {
    super(id, type, config, schema);
  }

  @Override
  public Schema autodetect() {
    if (StepType.from(type).equals(StepType.Loader)) {
      // Loader does not need to analyze the sql query
      BigQuery bigquery = BigQueryOptions.newBuilder().setProjectId(projectId).build().getService();
      Table table = bigquery.getTable(dataset, tablename);
      com.google.cloud.bigquery.Schema schema = table.getDefinition().getSchema();
      return BigQuerySchemaDetector.convertFromBigQuerySchema(schema);
    }

    // Connector
    String query = this.sql.replace(TABLE_PLACEHOLDER, "`" + getSQLTableName() + "`");
    return BigQuerySchemaDetector.autodetect(projectId, query);
  }

  @Override
  public void parseConfig(Map<String, Object> config) {
    projectId = StepConfigHelper.getConfig(config, PROJECT, "");
    dataset = StepConfigHelper.getConfig(config, DATASET, "");
    tablename = StepConfigHelper.getConfig(config, TABLENAME, "");
    sql = StepConfigHelper.getConfig(config, QUERY, "SELECT * FROM [TABLE] LIMIT 100");
  }

  @Override
  public Iterable<Row> iterateRows(Integer page, Integer pageSize) {
    String query = this.sql.replace(TABLE_PLACEHOLDER, "`" + getSQLTableName() + "`");
    TableResult result = BigQueryHelper.query(projectId, query);

    // Print all pages of the results.
    List<Row> results = new ArrayList<>();
    for (FieldValueList row : result.iterateAll()) {
      results.add(BigQueryHelper.bigQueryRowToBeamRow(row, this.schema));
    }

    return results;
  }

  private String getSQLTableName() {
    return projectId + "." + dataset + "." + tablename;
  }
}
