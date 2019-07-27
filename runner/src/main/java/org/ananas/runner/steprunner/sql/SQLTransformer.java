package org.ananas.runner.steprunner.sql;

import org.ananas.runner.kernel.StepRunner;
import org.ananas.runner.kernel.TransformerStepRunner;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.steprunner.sql.udf.*;
import org.apache.beam.repackaged.beam_sdks_java_extensions_sql.org.apache.calcite.linq4j.function.Parameter;
import org.apache.beam.sdk.extensions.sql.BeamSqlUdf;
import org.apache.beam.sdk.extensions.sql.SqlTransform;
import org.apache.beam.sdk.extensions.sql.impl.transform.BeamBuiltinAggregations;
import org.apache.beam.sdk.extensions.sql.impl.transform.agg.CovarianceFn;
import org.apache.beam.sdk.schemas.Schema;

import java.text.ParseException;
import java.text.SimpleDateFormat;

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
                    .registerUdf("falseIfNull", new NullableBooleanFn())
                    .registerUdf("zeroIfNull", new NullableBigDecimalFn())
                    .registerUdf("zeroIfNull", new NullableIntegerFn())
                    .registerUdf("emptyIfNull", new NullableStringFn())
                    .registerUdf("hash", new HashFn())
                    .registerUdf("todate", ToDateFn.class)
            );
  }
}
