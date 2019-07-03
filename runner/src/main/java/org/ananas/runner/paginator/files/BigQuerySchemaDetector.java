package org.ananas.runner.paginator.files;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableSchema;
import com.google.cloud.bigquery.LegacySQLTypeName;
import com.google.cloud.bigquery.StandardSQLTypeName;
import com.google.cloud.bigquery.TableResult;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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
                builder.addField(
                    field.getName(), FieldType.DATETIME.withMetadata("subtype", "DATE"));
              }
              if (fieldType.getStandardType().name().equals(StandardSQLTypeName.DATETIME.name())) {
                builder.addField(
                    field.getName(), FieldType.DATETIME.withMetadata("subtype", "DATETIME"));
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
                builder.addField(
                    field.getName(), FieldType.DATETIME.withMetadata("subtype", "TIME"));
              }
              if (fieldType.getStandardType().name().equals(StandardSQLTypeName.TIMESTAMP.name())) {
                builder.addField(
                    field.getName(), FieldType.DATETIME.withMetadata("subtype", "TIMESTAMP"));
              }
            });

    return builder.build();
  }

  public static TableSchema beamSchemaToTableSchema(Schema schema) {
    // See:
    // https://developers.google.com/resources/api-libraries/documentation/bigquery/v2/java/latest/com/google/api/services/bigquery/model/TableFieldSchema.html#setType-java.lang.String-
    TableSchema tableSchema = new TableSchema();
    List<TableFieldSchema> fields = new ArrayList<>();
    schema
        .getFields()
        .forEach(
            field -> {
              FieldType type = field.getType();
              if (checkBeamFieldTypeEqual(type, FieldType.BOOLEAN)) {
                fields.add(new TableFieldSchema().setName(field.getName()).setType("BOOL"));
              }
              if (checkBeamFieldTypeEqual(type, FieldType.BYTES)) {
                fields.add(new TableFieldSchema().setName(field.getName()).setType("BYTES"));
              }
              if (checkBeamFieldTypeEqual(type, FieldType.BYTE)) {
                fields.add(new TableFieldSchema().setName(field.getName()).setType("INTEGER"));
              }
              if (checkBeamFieldTypeEqual(type, FieldType.DATETIME)) {
                if (type.getMetadata("subtype").equals("DATE")) {
                  fields.add(new TableFieldSchema().setName(field.getName()).setType("DATE"));
                }
                if (type.getMetadata("subtype").equals("TIME")) {
                  fields.add(new TableFieldSchema().setName(field.getName()).setType("TIME"));
                }
                if (type.getMetadata("subtype").equals("DATETIME")) {
                  fields.add(new TableFieldSchema().setName(field.getName()).setType("DATETIME"));
                }
                if (type.getMetadata("subtype").equals("TIMESTAMP")) {
                  fields.add(new TableFieldSchema().setName(field.getName()).setType("TIMESTAMP"));
                }
              }
              if (checkBeamFieldTypeEqual(type, FieldType.DECIMAL)) {
                fields.add(new TableFieldSchema().setName(field.getName()).setType("FLOAT"));
              }
              if (checkBeamFieldTypeEqual(type, FieldType.DOUBLE)) {
                fields.add(new TableFieldSchema().setName(field.getName()).setType("FLOAT"));
              }
              if (checkBeamFieldTypeEqual(type, FieldType.FLOAT)) {
                fields.add(new TableFieldSchema().setName(field.getName()).setType("FLOAT"));
              }
              if (checkBeamFieldTypeEqual(type, FieldType.INT16)) {
                fields.add(new TableFieldSchema().setName(field.getName()).setType("INTEGER"));
              }
              if (checkBeamFieldTypeEqual(type, FieldType.INT32)) {
                fields.add(new TableFieldSchema().setName(field.getName()).setType("INTEGER"));
              }
              if (checkBeamFieldTypeEqual(type, FieldType.INT64)) {
                fields.add(new TableFieldSchema().setName(field.getName()).setType("INTEGER"));
              }
              if (checkBeamFieldTypeEqual(type, FieldType.STRING)) {
                fields.add(new TableFieldSchema().setName(field.getName()).setType("STRING"));
              }
            });

    tableSchema.setFields(fields);
    return tableSchema;
  }

  private static boolean checkBeamFieldTypeEqual(FieldType t1, FieldType t2) {
    return t1.withNullable(true).equals(t2) || t1.withNullable(false).equals(t2);
  }
}
