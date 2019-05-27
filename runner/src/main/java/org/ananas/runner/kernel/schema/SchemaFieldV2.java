package org.ananas.runner.kernel.schema;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import org.apache.beam.sdk.extensions.sql.impl.utils.CalciteUtils;
import org.apache.beam.sdk.schemas.Schema;

import java.io.Serializable;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SchemaFieldV2 implements Serializable {
  public String name;
  public String type;
  public String mode;
  public List<SchemaFieldV2> fields;

  public static SchemaFieldV2 Of(int index, String fieldName, Schema.FieldType type) {
    SchemaFieldV2 o = new SchemaFieldV2();
    o.name = fieldName;

    if (type.getTypeName().isCompositeType()) {
      o.fields = SchemaV2.Of(type.getRowSchema()).fields;
      o.type = "RECORD";
    } else {
      o.type = convert(type);
    }

    if (type.getTypeName().isCollectionType() && type.getCollectionElementType() != null) {
      o.mode = "REPEATED";
    }

    return o;
  }

  public static String convert(Schema.FieldType type) {
    return CalciteUtils.toSqlTypeName(type).getName();
  }
}
