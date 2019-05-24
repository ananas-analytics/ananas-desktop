package org.ananas.runner.model.core;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.ananas.runner.model.steps.commons.json.Jsonifier;
import org.apache.beam.repackaged.beam_sdks_java_core.org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;
import org.apache.commons.lang3.tuple.MutablePair;

@Data
public class Dataframe {
  public String id; //     string                            `json:"id"`
  public org.ananas.runner.model.schema.Schema
      schema; // map[string]map[string]interface{} `json:"schemas"`
  public List<List<Object>> data; // `json:"data"`
  public String message;

  private Dataframe(String id) {
    this.schema = new org.ananas.runner.model.schema.Schema();
    this.data = new ArrayList<>();
    this.id = id;
    this.message = null;
  }

  public static Dataframe OfRows(String id, Schema schema, Iterable<Row> data, String message) {
    Dataframe o = new Dataframe(id);
    o.id = id;
    Jsonifier json = Jsonifier.AsArray();
    if (data != null) {
      for (Row line : data) {
        List<Object> l = new ArrayList<>();
        if (line != null) {
          for (Object value : line.getValues()) {
            l.add(json.valueOfAny(value));
          }
          o.data.add(l);
        }
      }
    }
    o.schema = org.ananas.runner.model.schema.Schema.Of(schema);
    o.message = message;
    return o;
  }

  public static Dataframe Of(String id, MutableTriple<Schema, Iterable<Row>, String> triple) {
    return OfRows(id, triple.getLeft(), triple.getMiddle(), triple.getRight());
  }

  public static Dataframe Of(String id, MutablePair<Schema, Iterable<Row>> pair) {
    return OfRows(id, pair.getLeft(), pair.getRight(), null);
  }

  public static Dataframe Of(
      String id, Schema schema, Iterable<List<Object>> data, String message) {
    Dataframe o = new Dataframe(id);
    o.id = id;
    Jsonifier json = Jsonifier.AsArray();
    if (data != null) {
      for (List<Object> line : data) {
        List<Object> l = new ArrayList<>();
        if (line != null) {
          for (Object value : line) {
            l.add(json.valueOfAny(value));
          }
          o.data.add(l);
        }
      }
    }
    o.schema = org.ananas.runner.model.schema.Schema.Of(schema);
    o.message = message;
    return o;
  }
}
