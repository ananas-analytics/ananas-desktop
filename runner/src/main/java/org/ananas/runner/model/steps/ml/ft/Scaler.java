package org.ananas.runner.model.steps.ml.ft;

import smile.data.Attribute;
import smile.feature.FeatureTransform;
import smile.math.Math;

import java.io.Serializable;

/**
 * Scales all numeric variables into the range [0, 1].
 * If the dataset has outliers, normalization will certainly scale
 * the "normal" data to a very small interval. In this case, the
 * Winsorization procedure should be applied: values greater than the
 * specified upper limit are replaced with the upper limit, and those
 * below the lower limit are replace with the lower limit. Often, the
 * specified range is indicate in terms of percentiles of the original
 * distribution (like the 5th and 95th percentile).
 *
 * @author Haifeng Li
 */
public class Scaler extends FeatureTransform implements Serializable {
	private static final long serialVersionUID = 3744931536785591237L;
	/**
	 * Lower bound.
	 */
	protected double[] lo;
	/**
	 * Upper bound.
	 */
	protected double[] hi;

	/**
	 * Constructor. Inplace transformation.
	 */
	public Scaler() {

	}

	/**
	 * Constructor.
	 *
	 * @param copy If false, try to avoid a copy and do inplace scaling instead.
	 */
	public Scaler(boolean copy) {
		super(copy);
	}

	@Override
	public void learn(Attribute[] attributes, double[][] data) {
		this.lo = Math.colMin(data);
		this.hi = Math.colMax(data);

		for (int i = 0; i < this.lo.length; i++) {
			if (attributes[i].getType() != Attribute.Type.NUMERIC) {
				this.lo[i] = Double.NaN;
			} else {
				this.hi[i] -= this.lo[i];
				if (Math.isZero(this.hi[i])) {
					this.hi[i] = 1.0;
				}
			}
		}
	}

	@Override
	public double[] transform(double[] x) {
		if (x.length != this.lo.length) {
			throw new IllegalArgumentException(
					String.format("Invalid vector size %d, expected %d", x.length, this.lo.length));
		}

		double[] y = this.copy ? new double[x.length] : x;
		for (int i = 0; i < x.length; i++) {
			if (!Double.isNaN(this.lo[i])) {
				double yi = (x[i] - this.lo[i]) / this.hi[i];
				if (yi < 0.0) {
					yi = 0.0;
				}
				if (yi > 1.0) {
					yi = 1.0;
				}
				y[i] = yi;
			}
		}

		return y;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Scaler(");
		if (this.lo != null) {
			sb.append("\n");
			for (int i = 0; i < this.lo.length; i++) {
				sb.append(String.format("  [%.4f, %.4f]%n", this.lo[i], this.hi[i]));
			}
		}
		sb.append(")");
		return sb.toString();
	}
}
