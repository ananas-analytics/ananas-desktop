package org.ananas.runner.core.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;

/**
 * This translates a Row into a serializable object. Indeed, the Schema in Row object might a
 * circular reference.
 */
public class Jsonifier implements Serializable {

  private static final long serialVersionUID = 2641387357118783224L;
  private boolean RowAsArray;

  public static Jsonifier AsArray() {
    Jsonifier json = new Jsonifier();
    json.RowAsArray = true;
    return json;
  }

  public static Jsonifier AsMap() {
    Jsonifier json = new Jsonifier();
    json.RowAsArray = false;
    return json;
  }

  /**
   * Object value of af an array.
   *
   * @param fieldValue any field value ( might Row, ArrayList or Primitive)
   * @return the serializable value of a field
   */
  private Object valueOfArrayList(ArrayList fieldValue) {
    List l = (List) fieldValue;
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
  public Object valueOfAny(Object fieldValue) {
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
  public Object valueOfRow(Row row) {
    if (this.RowAsArray == true) {
      return valueOfRowAsArray(row);
    }
    return valueOfRowAsMap(row);
  }

  /**
   * Map value of af a row.
   *
   * @param row any row
   * @return the serializable value of a field
   */
  private Map valueOfRowAsMap(Row row) {
    Map h = new HashMap();
    for (Schema.Field field : row.getSchema().getFields()) {
      h.put(field.getName(), valueOfAny(row.getValue(field.getName())));
    }
    return h;
  }

  /**
   * Array value of af a row.
   *
   * @param row any row
   * @return the serializable value of a field
   */
  private List valueOfRowAsArray(Row row) {
    List h = new ArrayList();
    for (Object rowO : row.getValues()) {
      h.add(valueOfAny(rowO));
    }
    return h;
  }
}
