package org.ananas.runner.steprunner.gcs;

import static org.apache.beam.sdk.values.Row.toRow;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.FieldValue;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobId;
import com.google.cloud.bigquery.JobInfo;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.TableResult;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.IntStream;
import org.apache.beam.repackaged.beam_sdks_java_extensions_sql.org.apache.calcite.util.NlsString;
import org.apache.beam.sdk.extensions.sql.impl.utils.CalciteUtils;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.schemas.Schema.Field;
import org.apache.beam.sdk.schemas.Schema.FieldType;
import org.apache.beam.sdk.schemas.Schema.TypeName;
import org.apache.beam.sdk.values.Row;
import org.joda.time.DateTime;

public class BigQueryHelper {
  private BigQueryHelper() {}

  public static TableResult query(String projectId, String sql) {
    BigQuery bigquery = BigQueryOptions.newBuilder().setProjectId(projectId).build().getService();
    QueryJobConfiguration queryConfig =
        QueryJobConfiguration.newBuilder(sql).setUseLegacySql(false).build();
    JobId jobId = JobId.of(UUID.randomUUID().toString());
    Job queryJob = bigquery.create(JobInfo.newBuilder(queryConfig).setJobId(jobId).build());

    try {
      queryJob = queryJob.waitFor();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    // Check for errors
    if (queryJob == null) {
      throw new RuntimeException("Job no longer exists");
    } else if (queryJob.getStatus().getError() != null) {
      // You can also look at queryJob.getStatus().getExecutionErrors() for all
      // errors, not just the latest one.
      throw new RuntimeException(queryJob.getStatus().getError().toString());
    }

    // Get the results.
    TableResult result = null;
    try {
      return queryJob.getQueryResults();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public static Row bigQueryRowToBeamRow(FieldValueList row, Schema schema) {
    return IntStream.range(0, schema.getFieldCount())
        .mapToObj(
            idx -> {
              Field field = schema.getField(idx);
              FieldValue fieldValue = row.get(idx);
              return autoCast(field, fieldValue.getValue());
            })
        .collect(toRow(schema));
  }

  public static Object autoCast(Field field, Object rawObj) {
    if (rawObj == null) {
      if (!field.getType().getNullable()) {
        throw new IllegalArgumentException(String.format("Field %s not nullable", field.getName()));
      }
      return null;
    }

    FieldType type = field.getType();
    if (CalciteUtils.isStringType(type)) {
      if (rawObj instanceof NlsString) {
        return ((NlsString) rawObj).getValue();
      } else {
        return rawObj.toString();
      }
    } else if (CalciteUtils.isDateTimeType(type)) {
      // Internal representation of DateType in Calcite is convertible to Joda's Datetime.
      if (rawObj instanceof String) {
        try {
          Float timestamp = Float.parseFloat((String) rawObj);
          if (System.currentTimeMillis() / timestamp
              > 2) { // check if it is by seconds or milliseconds
            timestamp = timestamp * 1000;
          }
          return new DateTime(timestamp.longValue());
        } catch (NumberFormatException e) {
          // not a millisecond
          System.out.println(e);
        }
      }
      return new DateTime(rawObj);
    } else if (type.getTypeName().isNumericType()
        && ((rawObj instanceof String)
            || (rawObj instanceof BigDecimal && type.getTypeName() != TypeName.DECIMAL))) {
      String raw = rawObj.toString();
      switch (type.getTypeName()) {
        case BYTE:
          return Byte.valueOf(raw);
        case INT16:
          return Short.valueOf(raw);
        case INT32:
          return Integer.valueOf(raw);
        case INT64:
          return Long.valueOf(raw);
        case FLOAT:
          return Float.valueOf(raw);
        case DOUBLE:
          return Double.valueOf(raw);
        case DECIMAL:
          return BigDecimal.valueOf(Double.valueOf(raw));
        default:
          throw new UnsupportedOperationException(
              String.format("Column type %s is not supported yet!", type));
      }
    } else if (type.getTypeName().equals(FieldType.BOOLEAN.getTypeName())) {
      if (rawObj instanceof String) {
        return Boolean.valueOf((String) rawObj);
      }
      return rawObj;
    }
    return rawObj;
  }
}
