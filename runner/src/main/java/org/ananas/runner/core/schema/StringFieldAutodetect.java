package org.ananas.runner.core.schema;

import java.util.*;
import org.apache.beam.sdk.schemas.Schema;

public class StringFieldAutodetect implements SchemaAutodetect<String> {

  Map<Integer, List<String>> fields;
  Map<Integer, String> headers;

  public static StringFieldAutodetect of() {
    StringFieldAutodetect d = new StringFieldAutodetect();
    d.fields = new HashMap<>();
    d.headers = new HashMap<>();
    return d;
  }

  @Override
  public void add(int index, String name, String value) {
    this.headers.put(index, name);
    if (this.fields.get(index) == null) {
      this.fields.put(index, new ArrayList<>());
    }
    this.fields.get(index).add(value);
  }

  @Override
  public Schema autodetect() {
    Schema.Builder builder = Schema.builder();
    for (int i = 0; i < this.headers.size(); i++) {
      Iterator<String> it = this.fields.get(i).iterator();
      Schema.FieldType type = TypeInferer.inferType(it.next(), true);
      while (it.hasNext()) {
        type = TypeInferer.mergeType(type, TypeInferer.inferType(it.next(), true));
      }
      builder = builder.addNullableField(this.headers.get(i), type);
    }

    return builder.build();
  }
}
