package org.ananas.runner.legacy.steps.commons.json;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;
import org.bson.Document;

/**
 * This translates a Row into a serializable object. Indeed, the Schema in Row object might a
 * circular reference.
 */
public class Bsonifier implements Serializable {

  private static final long serialVersionUID = 3291638364118598839L;

  public static Bsonifier of() {
    return new Bsonifier();
  }

  /**
   * Object value of af an array.
   *
   * @param fieldValue any field value ( might Row, ArrayList or Primitive)
   * @return the serializable value of a field
   */
  private List valueOfArrayList(ArrayList fieldValue) {
    List l = fieldValue;
    List h = new ArrayList();
    for (Object se : l) {
      h.add(valueOfAny(se));
    }
    return h;
  }

  /**
   * Object value of af a field. If the field is a row we extract its fields as an array of object
   * value.
   *
   * @param fieldValue any field value ( might Row, ArrayList or Primitive)
   * @return the serializable value of a field
   */
  private Object valueOfAny(Object fieldValue) {
    if (fieldValue instanceof ArrayList) {
      return valueOfArrayList((ArrayList) fieldValue);
    }
    if (fieldValue instanceof Row) {
      return valueOfRow((Row) fieldValue);
    }
    return fieldValue;
  }

  /**
   * Object value of af a row.
   *
   * @param row any row
   * @return the serializable value of a field
   */
  Document valueOfRow(Row row) {
    return valueOfRowAsMap(row);
  }

  /**
   * Map value of af a row.
   *
   * @param row any row
   * @return the serializable value of a field
   */
  private Document valueOfRowAsMap(Row row) {
    Document h = new Document();
    for (Schema.Field field : row.getSchema().getFields()) {
      h.put(field.getName(), valueOfAny(row.getValue(field.getName())));
    }
    return h;
  }
}
