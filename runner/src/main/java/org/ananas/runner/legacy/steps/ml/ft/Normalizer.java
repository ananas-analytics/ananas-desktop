package org.ananas.runner.legacy.steps.ml.ft;

import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smile.data.Attribute;
import smile.feature.FeatureTransform;
import smile.math.Math;

/**
 * Normalize samples individually to unit norm. Each sample (i.e. each row of the data matrix) with
 * at least one non zero component is rescaled independently of other samples so that its norm (L1
 * or L2) equals one.
 *
 * <p>Scaling inputs to unit norms is a common operation for text classification or clustering for
 * instance.
 *
 * @author Haifeng Li
 */
public class Normalizer extends FeatureTransform implements Serializable {
  private static final Logger logger = LoggerFactory.getLogger(smile.feature.Normalizer.class);
  private static final long serialVersionUID = 8491804349352189292L;

  /** The types of data scaling. */
  public static enum Norm {
    /** L1 vector norm. */
    L1,
    /** L2 vector norm. */
    L2,
    /** L-infinity vector norm. Maximum absolute value. */
    Inf
  }

  /** The type of norm . */
  private smile.feature.Normalizer.Norm norm = smile.feature.Normalizer.Norm.L2;

  /** Default constructor with L2 norm. */
  public Normalizer() {}

  /**
   * Constructor with L2 norm.
   *
   * @param copy If false, try to avoid a copy and do inplace scaling instead.
   */
  public Normalizer(boolean copy) {
    super(copy);
  }

  /**
   * Constructor.
   *
   * @param norm The norm to use to normalize each non zero sample.
   */
  public Normalizer(smile.feature.Normalizer.Norm norm) {
    this.norm = norm;
  }

  /**
   * Constructor.
   *
   * @param norm The norm to use to normalize each non zero sample.
   * @param copy If false, try to avoid a copy and do inplace scaling instead.
   */
  public Normalizer(smile.feature.Normalizer.Norm norm, boolean copy) {
    super(copy);
    this.norm = norm;
  }

  @Override
  public void learn(Attribute[] attributes, double[][] data) {
    logger.info("Normalizer is stateless and learn() does nothing.");
  }

  @Override
  public double[] transform(double[] x) {
    double scale;

    switch (this.norm) {
      case L1:
        scale = Math.norm1(x);
        break;
      case L2:
        scale = Math.norm2(x);
        break;
      case Inf:
        scale = Math.normInf(x);
        break;
      default:
        throw new IllegalStateException("Unknown type of norm: " + this.norm);
    }

    double[] y = this.copy ? new double[x.length] : x;
    if (Math.isZero(scale)) {
      if (y != x) {
        System.arraycopy(x, 0, y, 0, x.length);
      }
    } else {
      for (int i = 0; i < x.length; i++) {
        y[i] = x[i] / scale;
      }
    }

    return y;
  }

  @Override
  public String toString() {
    return "Normalizer()";
  }
}
