package org.ananas.runner.legacy.steps.ml;

import java.io.Serializable;
import org.apache.beam.sdk.schemas.Schema;

public interface Predictor<R> extends Serializable {

  /**
   * Predicts the class label of an instance.
   *
   * @param x the instance to be classified.
   * @return the predicted class label.
   */
  R predict(double[] x);

  /**
   * Check Model state
   *
   * @return true if legacy is consistent
   */
  default boolean check() {
    return true;
  }

  /**
   * Return the predicted type
   *
   * @return
   */
  Schema.FieldType getPredictedType();
}
