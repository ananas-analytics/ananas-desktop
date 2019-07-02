package org.ananas.runner.paginator.files;

import com.google.cloud.bigquery.LegacySQLTypeName;
import com.google.cloud.bigquery.StandardSQLTypeName;
import com.google.cloud.bigquery.TableResult;
import java.io.Serializable;
import org.ananas.runner.steprunner.gcs.BigQueryHelper;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.schemas.Schema.Builder;
import org.apache.beam.sdk.schemas.Schema.FieldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BigQuerySchemaDetector implements Serializable {

  private static final long serialVersionUID = -2013658411275898166L;

  private static final Logger LOGGER = LoggerFactory.getLogger(BigQuerySchemaDetector.class);

  public static Schema autodetect(String projectId, String query) {
    TableResult result = BigQueryHelper.query(projectId, query);
    return extractSchema(result);
  }

  public static Schema extractSchema(TableResult result) {
    return convertFromBigQuerySchema(result.getSchema());
  }

  public static Schema convertFromBigQuerySchema(com.google.cloud.bigquery.Schema bqSchema) {
    Builder builder = Schema.builder();

    bqSchema
        .getFields()
        .forEach(
            field -> {
              LegacySQLTypeName fieldType = field.getType();
              if (fieldType.getStandardType().name().equals(StandardSQLTypeName.BOOL.name())) {
                builder.addField(field.getName(), FieldType.BOOLEAN);
              }
              if (fieldType.getStandardType().name().equals(StandardSQLTypeName.BYTES.name())) {
                builder.addField(field.getName(), FieldType.BYTES);
              }
              if (fieldType.getStandardType().name().equals(StandardSQLTypeName.DATE.name())) {
                builder.addField(field.getName(), FieldType.DATETIME);
              }
              if (fieldType.getStandardType().name().equals(StandardSQLTypeName.DATETIME.name())) {
                builder.addField(field.getName(), FieldType.DATETIME);
              }
              if (fieldType.getStandardType().name().equals(StandardSQLTypeName.FLOAT64.name())) {
                builder.addField(field.getName(), FieldType.FLOAT);
              }
              if (fieldType.getStandardType().name().equals(StandardSQLTypeName.INT64.name())) {
                builder.addField(field.getName(), FieldType.INT64);
              }
              if (fieldType.getStandardType().name().equals(StandardSQLTypeName.NUMERIC.name())) {
                builder.addField(field.getName(), FieldType.DECIMAL);
              }
              if (fieldType.getStandardType().name().equals(StandardSQLTypeName.STRING.name())) {
                builder.addField(field.getName(), FieldType.STRING);
              }
              if (fieldType.getStandardType().name().equals(StandardSQLTypeName.TIME.name())) {
                builder.addField(field.getName(), FieldType.DATETIME);
              }
              if (fieldType.getStandardType().name().equals(StandardSQLTypeName.TIMESTAMP.name())) {
                builder.addField(field.getName(), FieldType.DATETIME);
              }
            });

    return builder.build();
  }
}
