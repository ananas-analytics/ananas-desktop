package org.ananas.runner.model.steps.ml.ft;

import java.io.Serializable;
import smile.data.Attribute;
import smile.math.Math;
import smile.sort.QuickSelect;

/**
 * Robustly standardizes numeric feature by subtracting the median and dividing by the IQR.
 *
 * @author Haifeng Li
 */
public class RobustStandardizer extends Standardizer implements Serializable {

  private static final long serialVersionUID = -916263307136609544L;

  /** Constructor. */
  public RobustStandardizer() {}

  /**
   * Constructor.
   *
   * @param copy If false, try to avoid a copy and do inplace scaling instead.
   */
  public RobustStandardizer(boolean copy) {
    super(copy);
  }

  @Override
  public void learn(Attribute[] attributes, double[][] data) {
    int n = data.length;
    int p = data[0].length;

    this.mu = new double[p];
    this.std = new double[p];
    double[] x = new double[n];

    for (int j = 0; j < p; j++) {
      if (attributes[j].getType() != Attribute.Type.NUMERIC) {
        this.mu[j] = Double.NaN;
      } else {
        for (int i = 0; i < n; i++) {
          x[i] = data[i][j];
        }

        this.mu[j] = QuickSelect.median(x);
        this.std[j] = QuickSelect.q3(x) - QuickSelect.q1(x);
        if (Math.isZero(this.std[j])) {
          throw new IllegalArgumentException(
              "Column " + j + " has constant values between Q1 and Q3.");
        }
      }
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("RobustStandardizer(");
    if (this.mu != null) {
      sb.append("\n");
      for (int i = 0; i < this.mu.length; i++) {
        sb.append(String.format("  [%.4f, %.4f]%n", this.mu[i], this.std[i]));
      }
    }
    sb.append(")");
    return sb.toString();
  }
}
