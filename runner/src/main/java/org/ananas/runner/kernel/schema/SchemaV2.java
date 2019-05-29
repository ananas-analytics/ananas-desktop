package org.ananas.runner.kernel.schema;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class SchemaV2 implements Serializable {

  public List<SchemaFieldV2> fields;

  public static SchemaV2 Of(org.apache.beam.sdk.schemas.Schema schema) {
    SchemaV2 s = new SchemaV2();
    s.fields = new ArrayList<>();
    if (schema == null) {
      return s;
    }
    for (org.apache.beam.sdk.schemas.Schema.Field f : schema.getFields()) {
      s.fields.add(SchemaFieldV2.Of(f.getName(), f.getType()));
    }
    return s;
  }


  public static org.apache.beam.sdk.schemas.Schema fieldsToBeamSchema(List<SchemaFieldV2> fields) {
    if (fields == null) {
      throw new IllegalArgumentException();
    }
     org.apache.beam.sdk.schemas.Schema.Builder builder =
        org.apache.beam.sdk.schemas.Schema.builder();
    for (SchemaFieldV2 field : fields) {
      builder.addField(field.toBeamField());
    }
    return builder.build();
  }

  public org.apache.beam.sdk.schemas.Schema toBeamSchema() {
    return fieldsToBeamSchema(fields);
  }
}
