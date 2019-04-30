package org.ananas.runner.model.steps.ml.ft;

import smile.data.Attribute;
import smile.feature.FeatureTransform;
import smile.math.Math;

import java.io.Serializable;

/**
 * Standardizes numeric feature to 0 mean and unit variance.
 * Standardization makes an assumption that the data follows
 * a Gaussian distribution and are also not robust when outliers present.
 * A robust alternative is to subtract the median and divide by the IQR
 * by <code>RobustStandardizer</code>.
 *
 * @author Haifeng Li
 */
public class Standardizer extends FeatureTransform implements Serializable {
	private static final long serialVersionUID = 7102795383144090252L;
	/**
	 * Mean or median.
	 */
	double[] mu;
	/**
	 * Standard deviation or IQR.
	 */
	double[] std;

	/**
	 * Constructor.
	 */
	public Standardizer() {

	}

	/**
	 * Constructor.
	 *
	 * @param copy If false, try to avoid a copy and do inplace scaling instead.
	 */
	public Standardizer(boolean copy) {
		super(copy);
	}

	@Override
	public void learn(Attribute[] attributes, double[][] data) {
		this.mu = Math.colMeans(data);
		this.std = Math.colSds(data);

		for (int i = 0; i < this.std.length; i++) {
			if (attributes[i].getType() != Attribute.Type.NUMERIC) {
				this.mu[i] = Double.NaN;
			}

			if (Math.isZero(this.std[i])) {
				this.std[i] = 1.0;
			}
		}
	}

	@Override
	public double[] transform(double[] x) {
		if (x.length != this.mu.length) {
			throw new IllegalArgumentException(
					String.format("Invalid vector size %d, expected %d", x.length, this.mu.length));
		}

		double[] y = this.copy ? new double[x.length] : x;
		for (int i = 0; i < x.length; i++) {
			if (!Double.isNaN(this.mu[i])) {
				y[i] = (x[i] - this.mu[i]) / this.std[i];
			} else {
				y[i] = x[i];
			}
		}

		return y;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Standardizer(");
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

