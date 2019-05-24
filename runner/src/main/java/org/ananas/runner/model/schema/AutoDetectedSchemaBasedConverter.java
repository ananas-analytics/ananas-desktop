package org.ananas.runner.model.schema;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.ananas.runner.model.steps.commons.RowConverter;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;

public class AutoDetectedSchemaBasedConverter implements RowConverter, Serializable {

  private static final int MAX_SCHEMA_RESOLUTION_ATTEMPT = 1000;
  private static final long serialVersionUID = 1182221897758415405L;
  private Map<String, Schema.FieldType> types;

  private SchemaBasedRowConverter delegate;
  private int attemps;
  private Schema finalSchema;

  private AutoDetectedSchemaBasedConverter() {
    this.types = new ConcurrentHashMap<>();
    this.finalSchema = Schema.of().builder().build();
    this.delegate = SchemaBasedRowConverter.of(this.finalSchema);
    this.attemps = 0;
  }

  @Override
  public Row convertMap(Map<String, Object> o) {
    updateSchema(o);
    return this.delegate.convertMap(o);
  }

  private void updateSchema(Map<String, Object> o) {
    if (this.attemps < MAX_SCHEMA_RESOLUTION_ATTEMPT) {
      this.attemps++;
      JsonAutodetect.inferTypes(this.types, o, false);
      this.finalSchema = JsonAutodetect.buildSchema(this.types);
    }
  }

  public Schema getFinalSchema() {
    return this.finalSchema;
  }

  public static AutoDetectedSchemaBasedConverter of() {
    return new AutoDetectedSchemaBasedConverter();
  }
}
