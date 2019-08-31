package org.ananas.runner.core.schema;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class Schema implements Serializable {

  public List<SchemaField> fields;

  public static Schema of(org.apache.beam.sdk.schemas.Schema schema) {
    Schema s = new Schema();
    s.fields = new ArrayList<>();
    if (schema == null) {
      return s;
    }
    for (org.apache.beam.sdk.schemas.Schema.Field f : schema.getFields()) {
      s.fields.add(SchemaField.Of(f.getName(), f.getType()));
    }
    return s;
  }

  public static org.apache.beam.sdk.schemas.Schema fieldsToBeamSchema(List<SchemaField> fields) {
    if (fields == null) {
      throw new IllegalArgumentException();
    }
    org.apache.beam.sdk.schemas.Schema.Builder builder =
        org.apache.beam.sdk.schemas.Schema.builder();
    for (SchemaField field : fields) {
      builder.addField(field.toBeamField());
    }
    return builder.build();
  }

  public org.apache.beam.sdk.schemas.Schema toBeamSchema() {
    return fieldsToBeamSchema(fields);
  }
}
