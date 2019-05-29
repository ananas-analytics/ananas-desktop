package org.ananas.runner.kernel.schema;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import org.apache.beam.repackaged.beam_sdks_java_extensions_sql.org.apache.calcite.sql.type.SqlTypeName;
import org.apache.beam.sdk.extensions.sql.impl.utils.CalciteUtils;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.schemas.Schema.FieldType;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class SchemaFieldV2 implements Serializable {

  public String name;
  public String type; // beam schema field type, ARRAY, MAP, and ROW maps to RECORD
  public String mode; // NULLABLE (default), REPEATED (for beam schema ARRAY type)
  public List<SchemaFieldV2> fields; // only available for RECORD type

  /**
   *
   *
   * @param fieldName the name of the field
   * @param type, the type of the field. Supports all beam field types
   * @return
   */
  public static SchemaFieldV2 Of(String fieldName, Schema.FieldType type) {
    SchemaFieldV2 o = new SchemaFieldV2();
    o.name = fieldName;
    if (type.getTypeName().isCompositeType()) {
      o.type = "RECORD";
      o.fields = SchemaV2.Of(type.getRowSchema()).fields;
    } else if (type.getTypeName().isCollectionType()){
      o.mode = "REPEATED";
      o.fields = SchemaV2.Of(type.getCollectionElementType().getRowSchema()).fields;
      if (type.getCollectionElementType().getTypeName().isCompositeType()) {
        o.type = "RECORD";
      } else {
        o.type = convert(type.getCollectionElementType());
      }
    } else {
      o.type = convert(type);
    }
    return o;
  }

  public static String convert(Schema.FieldType type) {
    // convert beam field type to SQL type
    // NOTE: need to convert it back in toBeamField() method
    return CalciteUtils.toSqlTypeName(type).getName();
  }


  public org.apache.beam.sdk.schemas.Schema.Field toBeamField() {
    String beamType = type;

    try {
      // convert SQL type to beam type
      beamType = CalciteUtils.toFieldType(SqlTypeName.valueOf(type)).getTypeName().name();
    } catch (IllegalArgumentException e) {
      // DO nothing
    }

    try {
      Schema.TypeName typeName = Schema.TypeName.valueOf(beamType);
      if (mode != null && mode.equals("REPEATED")) {
        return Schema.Field.of(name, Schema.FieldType.array(Schema.FieldType.of(typeName)));
      } else {
        return Schema.Field.of(name, Schema.FieldType.of(typeName))
          .withNullable(mode == null || mode.equals("NULLABLE"));
      }
    } catch ( IllegalArgumentException e ) {
      if (type.equals("RECORD")) {
        if (mode != null && mode.equals("REPEATED")) {
          return Schema.Field.of(name, Schema.FieldType.array(Schema.FieldType.row(SchemaV2.fieldsToBeamSchema(fields))));
        } else {
          return Schema.Field.of(name, Schema.FieldType.row(SchemaV2.fieldsToBeamSchema(fields)))
            .withNullable(mode == null || mode.equals("NULLABLE"));
        }
      }
      throw new IllegalArgumentException(e);
    }
  }
}
