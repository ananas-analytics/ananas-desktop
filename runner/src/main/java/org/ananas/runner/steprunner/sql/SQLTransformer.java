package org.ananas.runner.steprunner.sql;

import org.ananas.runner.kernel.StepRunner;
import org.ananas.runner.kernel.TransformerStepRunner;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.steprunner.sql.udf.HashFn;
import org.ananas.runner.steprunner.sql.udf.NullableBigDecimalFn;
import org.ananas.runner.steprunner.sql.udf.NullableBooleanFn;
import org.ananas.runner.steprunner.sql.udf.NullableIntegerFn;
import org.ananas.runner.steprunner.sql.udf.NullableStringFn;
import org.apache.beam.sdk.extensions.sql.SqlTransform;

public class SQLTransformer extends TransformerStepRunner {

  private static final long serialVersionUID = -64482147086202330L;

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
                    .registerUdf("HASH", new HashFn()));
  }
}
