package org.ananas.runner.kernel.schema;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import org.apache.beam.repackaged.beam_sdks_java_extensions_sql.org.apache.calcite.sql.type.SqlTypeName;
import org.apache.beam.sdk.extensions.sql.impl.utils.CalciteUtils;
import org.apache.beam.sdk.schemas.Schema.FieldType;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class SchemaField implements Serializable {

  public String name;
  public String type; // beam schema field type, ARRAY, MAP, and ROW maps to RECORD
  public String mode; // NULLABLE (default), REPEATED (for beam schema ARRAY type)
  public List<SchemaField> fields; // only available for RECORD type

  public static FieldType normalizeFieldType(
      org.apache.beam.sdk.schemas.Schema.FieldType fieldType) {

    if (fieldType.getTypeName().isLogicalType()) {
      org.apache.beam.sdk.schemas.Schema.Field f = SchemaField.Of("none", fieldType).toBeamField();
      return f.getType();
    } else {
      return fieldType;
    }
  }

  public static org.apache.beam.sdk.schemas.Schema normalizeSchema(
      org.apache.beam.sdk.schemas.Schema schema) {
    org.apache.beam.sdk.schemas.Schema.Builder builder =
        new org.apache.beam.sdk.schemas.Schema.Builder();
    schema
        .getFields()
        .forEach(
            field -> {
              builder.addField(field.getName(), normalizeFieldType(field.getType()));
            });
    return builder.build();
  }

  /**
   * @param fieldName the name of the field
   * @param type, the type of the field. Supports all beam field types
   * @return
   */
  public static SchemaField Of(
      String fieldName, org.apache.beam.sdk.schemas.Schema.FieldType type) {
    SchemaField o = new SchemaField();
    o.name = fieldName;
    if (type.getTypeName().isCompositeType()) {
      o.type = "RECORD";
      o.fields = Schema.of(type.getRowSchema()).fields;
    } else if (type.getTypeName().isCollectionType()) {
      o.mode = "REPEATED";
      o.fields = Schema.of(type.getCollectionElementType().getRowSchema()).fields;
      if (type.getCollectionElementType().getTypeName().isCompositeType()) {
        o.type = "RECORD";
      } else {
        o.type = fromBeamField(type.getCollectionElementType());
      }
    } else {
      o.type = fromBeamField(type);
    }
    return o;
  }

  public static String fromBeamField(org.apache.beam.sdk.schemas.Schema.FieldType type) {
    // convert beam field type to SQL type
    // NOTE: need to convert it back in toBeamField() method
    if (type.getTypeName().name().equals(FieldType.DATETIME.getTypeName().name())) {
      String subType = type.getMetadataString("subtype");
      if ("DATETIME".equals(subType) || "TIMESTAMP".equals(subType) || "TS".equals(subType)) {
        return SqlTypeName.TIMESTAMP.getName();
      }
      if ("DATE".equals(subType)) {
        return SqlTypeName.DATE.getName();
      }
      if ("TIME".equals(subType)) {
        return SqlTypeName.TIME.getName();
      }
    }
    String sqlType = CalciteUtils.toSqlTypeName(type).getName();
    if (sqlType.equals(SqlTypeName.CHAR.getName())) {
      return SqlTypeName.VARCHAR.getName();
    }
    return sqlType;
  }

  public org.apache.beam.sdk.schemas.Schema.Field toBeamField() {
    String beamType = type;

    if ("DATE".equals(type)) {
      return org.apache.beam.sdk.schemas.Schema.Field.of(name, CalciteUtils.DATE);
    }

    if ("TIME".equals(type)) {
      return org.apache.beam.sdk.schemas.Schema.Field.of(name, CalciteUtils.TIME);
    }

    if ("TS".equals(type)) {
      return org.apache.beam.sdk.schemas.Schema.Field.of(name, CalciteUtils.TIMESTAMP);
    }

    try {
      // convert SQL type to beam type
      beamType = CalciteUtils.toFieldType(SqlTypeName.valueOf(type)).getTypeName().name();
    } catch (IllegalArgumentException e) {
      // DO nothing
    }

    try {
      org.apache.beam.sdk.schemas.Schema.TypeName typeName =
          org.apache.beam.sdk.schemas.Schema.TypeName.valueOf(beamType);
      if (mode != null && mode.equals("REPEATED")) {
        return org.apache.beam.sdk.schemas.Schema.Field.of(
            name,
            org.apache.beam.sdk.schemas.Schema.FieldType.array(
                org.apache.beam.sdk.schemas.Schema.FieldType.of(typeName)));
      } else {
        return org.apache.beam.sdk.schemas.Schema.Field.of(
                name, org.apache.beam.sdk.schemas.Schema.FieldType.of(typeName))
            .withNullable(mode == null || mode.equals("NULLABLE"));
      }
    } catch (IllegalArgumentException e) {
      if (type.equals("RECORD")) {
        if (mode != null && mode.equals("REPEATED")) {
          return org.apache.beam.sdk.schemas.Schema.Field.of(
              name,
              org.apache.beam.sdk.schemas.Schema.FieldType.array(
                  org.apache.beam.sdk.schemas.Schema.FieldType.row(
                      Schema.fieldsToBeamSchema(fields))));
        } else {
          return org.apache.beam.sdk.schemas.Schema.Field.of(
                  name,
                  org.apache.beam.sdk.schemas.Schema.FieldType.row(
                      Schema.fieldsToBeamSchema(fields)))
              .withNullable(mode == null || mode.equals("NULLABLE"));
        }
      }
      throw new IllegalArgumentException(e);
    }
  }
}
