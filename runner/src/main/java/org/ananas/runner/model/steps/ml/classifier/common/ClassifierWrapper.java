package org.ananas.runner.model.steps.ml.classifier.common;

import com.google.common.base.Preconditions;
import java.io.Serializable;
import org.ananas.runner.model.steps.ml.Predictor;
import org.apache.beam.sdk.schemas.Schema;
import smile.classification.Classifier;
import smile.feature.FeatureTransform;

public class ClassifierWrapper<T extends Classifier<double[]>>
    implements Predictor<Integer>, Serializable {

  private static final long serialVersionUID = 7423719663600179750L;
  private T subject;
  private FeatureTransform ft;

  public ClassifierWrapper(T subject, FeatureTransform ft) {
    Preconditions.checkNotNull(subject);
    this.subject = subject;
    this.ft = ft;
  }

  public static <U extends Classifier<double[]>> ClassifierWrapper of(U s, FeatureTransform ft) {
    return new ClassifierWrapper<>(s, ft);
  }

  @Override
  public Integer predict(double[] x) {
    if (this.ft == null) {
      return this.subject.predict(x);
    }
    return this.subject.predict(this.ft.transform(x));
  }

  @Override
  public Schema.FieldType getPredictedType() {
    return Schema.FieldType.INT32;
  }
}
