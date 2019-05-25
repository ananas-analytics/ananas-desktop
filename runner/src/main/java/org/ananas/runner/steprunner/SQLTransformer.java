package org.ananas.runner.steprunner;

import org.ananas.runner.kernel.StepRunner;
import org.ananas.runner.kernel.TransformerStepRunner;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.model.steps.sql.udf.HashFn;
import org.ananas.runner.model.steps.sql.udf.NullableBigDecimalFn;
import org.ananas.runner.model.steps.sql.udf.NullableBooleanFn;
import org.ananas.runner.model.steps.sql.udf.NullableIntegerFn;
import org.ananas.runner.model.steps.sql.udf.NullableStringFn;
import org.apache.beam.sdk.extensions.sql.SqlTransform;

public class SQLTransformer extends TransformerStepRunner {

  public static final String CONFIG_SQL = "sql";

  public SQLTransformer(Step step, StepRunner previous) {
    super(step, previous);
  }

  public void build() {
    String statement = (String) this.step.config.get(CONFIG_SQL);
    this.output =
        previous
            .getOutput()
            .apply(
                "sql transform",
                SqlTransform.query(statement)
                    .registerUdf("falseIfNull", new NullableBooleanFn())
                    .registerUdf("zeroIfNull", new NullableBigDecimalFn())
                    .registerUdf("zeroIfNull", new NullableIntegerFn())
                    .registerUdf("emptyIfNull", new NullableStringFn())
                    .registerUdf("hash", new HashFn()));
  }
}
