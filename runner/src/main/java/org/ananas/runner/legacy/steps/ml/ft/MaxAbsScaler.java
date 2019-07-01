package org.ananas.runner.legacy.steps.ml.ft;

import java.io.Serializable;
import smile.data.Attribute;
import smile.feature.FeatureTransform;
import smile.math.Math;

/**
 * Scales each feature by its maximum absolute value. This class scales and translates each feature
 * individually such that the maximal absolute value of each feature in the training set will be
 * 1.0. It does not shift/center the data, and thus does not destroy any sparsity.
 *
 * @author Haifeng Li
 */
public class MaxAbsScaler extends FeatureTransform implements Serializable {
  private static final long serialVersionUID = -1889791747716112156L;
  /** Scaling factor. */
  private double[] scale;

  /** Constructor. */
  public MaxAbsScaler() {}

  /**
   * Constructor.
   *
   * @param copy If false, try to avoid a copy and do inplace scaling instead.
   */
  public MaxAbsScaler(boolean copy) {
    super(copy);
  }

  @Override
  public void learn(Attribute[] attributes, double[][] data) {
    int n = data.length;
    int p = data[0].length;
    this.scale = new double[p];
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < p; j++) {
        if (attributes[j].getType() == Attribute.Type.NUMERIC) {
          double abs = Math.abs(data[i][j]);
          if (this.scale[j] < abs) {
            this.scale[j] = abs;
          }
        }
      }
    }

    for (int i = 0; i < this.scale.length; i++) {
      if (Math.isZero(this.scale[i])) {
        this.scale[i] = 1.0;
      }
    }
  }

  /**
   * Scales each feature by its maximum absolute value.
   *
   * @param x a vector to be scaled. The vector will be modified on output.
   * @return the input vector.
   */
  @Override
  public double[] transform(double[] x) {
    if (x.length != this.scale.length) {
      throw new IllegalArgumentException(
          String.format("Invalid vector size %d, expected %d", x.length, this.scale.length));
    }

    double[] y = this.copy ? new double[x.length] : x;
    for (int i = 0; i < x.length; i++) {
      y[i] = x[i] / this.scale[i];
    }

    return y;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("MaxAbsScaler(");
    if (this.scale != null) {
      if (this.scale.length > 0) {
        sb.append(String.format("%.4f", this.scale[0]));
      }

      for (int i = 1; i < this.scale.length; i++) {
        sb.append(String.format(", %.4f", this.scale[i]));
      }
    }
    sb.append(")");
    return sb.toString();
  }
}
