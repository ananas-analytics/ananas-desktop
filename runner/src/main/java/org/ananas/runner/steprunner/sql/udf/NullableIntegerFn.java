package org.ananas.runner.steprunner.sql.udf;

import java.math.BigDecimal;
import org.apache.beam.sdk.transforms.SerializableFunction;

public class NullableIntegerFn implements SerializableFunction<BigDecimal, BigDecimal> {
  private static final long serialVersionUID = -3952311279068727452L;

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
