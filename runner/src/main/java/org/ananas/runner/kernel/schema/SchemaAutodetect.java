package org.ananas.runner.kernel.schema;

import org.apache.beam.sdk.schemas.Schema;

/**
 * Autodetect type of field with type T
 *
 * @param <T>
 */
public interface SchemaAutodetect<T> {

  /**
   * Autodetect type of field with type T
   *
   * @param index
   * @param fieldName
   * @param fieldValue value whose type is to be autodetected
   */
  void add(int index, String fieldName, T fieldValue);

  /**
   * Autodetect schemas
   *
   * @return the autodetected schemas
   */
  Schema autodetect();
}
