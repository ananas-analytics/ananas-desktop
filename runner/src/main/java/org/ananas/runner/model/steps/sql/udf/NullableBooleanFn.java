package org.ananas.runner.model.steps.sql.udf;


import org.apache.beam.sdk.transforms.SerializableFunction;

public class NullableBooleanFn implements SerializableFunction<Boolean, Boolean> {
	private static final long serialVersionUID = -7365904166914169286L;

	@Override
	public Boolean apply(Boolean input) {
		if (input == null) {
			return false;
		} else {
			return input;
		}
	}
}
