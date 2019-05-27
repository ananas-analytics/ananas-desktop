package org.ananas.runner.kernel.schema;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class SchemaV2 implements Serializable {

  public List<SchemaFieldV2> fields;

  public static SchemaV2 Of(org.apache.beam.sdk.schemas.Schema schema) {
    SchemaV2 s = new SchemaV2();
    s.fields = new ArrayList<>();
    if (schema == null) {
      return s;
    }
    int i = 1;
    for (org.apache.beam.sdk.schemas.Schema.Field f : schema.getFields()) {
      s.fields.add(SchemaFieldV2.Of(i, f.getName(), f.getType()));
      i++;
    }
    return s;
  }
}
