package org.ananas.runner.model.steps.sql.udf;

import org.apache.beam.sdk.transforms.SerializableFunction;

public class NullableStringFn implements SerializableFunction<String, String> {
  private static final long serialVersionUID = -8064028600455474440L;

  @Override
  public String apply(String input) {
    if (input == null) {
      return "";
    } else {
      return input;
    }
  }
}
