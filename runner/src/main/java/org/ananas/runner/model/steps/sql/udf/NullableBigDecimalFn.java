package org.ananas.runner.model.steps.sql.udf;

import org.apache.beam.sdk.transforms.SerializableFunction;

import java.math.BigDecimal;

public class NullableBigDecimalFn implements SerializableFunction<BigDecimal, BigDecimal> {
	private static final long serialVersionUID = -6640803101378748810L;

	private static final BigDecimal ZERO_BIGDECIMAL = new BigDecimal("0.00");

	@Override
	public BigDecimal apply(BigDecimal input) {
		if (input == null) {
			return ZERO_BIGDECIMAL;
		} else {
			return input;
		}
	}
}
