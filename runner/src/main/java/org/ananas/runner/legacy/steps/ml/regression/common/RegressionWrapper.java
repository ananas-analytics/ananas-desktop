package org.ananas.runner.legacy.steps.ml.regression.common;

import com.google.common.base.Preconditions;
import java.io.Serializable;
import org.ananas.runner.legacy.steps.ml.Predictor;
import org.apache.beam.sdk.schemas.Schema;
import smile.feature.FeatureTransform;
import smile.regression.Regression;

public class RegressionWrapper<T extends Regression<double[]>>
    implements Predictor<Double>, Serializable {

  private static final long serialVersionUID = 7423719663600179750L;
  private T subject;
  private FeatureTransform standardizer;

  public RegressionWrapper(T subject, FeatureTransform standardizer) {
    Preconditions.checkNotNull(subject);
    this.subject = subject;
    this.standardizer = standardizer;
  }

  public static <U extends Regression<double[]>> RegressionWrapper of(
      U s, FeatureTransform standardizer) {
    return new RegressionWrapper<>(s, standardizer);
  }

  public T getSubject() {

    return this.subject;
  }

  @Override
  public Double predict(double[] x) {
    if (this.standardizer != null) {
      return this.subject.predict(this.standardizer.transform(x));
    }
    return this.subject.predict(x);
  }

  @Override
  public Schema.FieldType getPredictedType() {
    return Schema.FieldType.DOUBLE;
  }
}
