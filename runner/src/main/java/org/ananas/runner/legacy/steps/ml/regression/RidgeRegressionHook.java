package org.ananas.runner.legacy.steps.ml.regression;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.legacy.core.StepConfig;
import org.ananas.runner.legacy.steps.ml.MLModelTrainer;
import org.ananas.runner.legacy.steps.ml.regression.common.RegressionLinearHook;
import org.ananas.runner.misc.MutableQuadruple;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;
import smile.data.Attribute;
import smile.math.Math;
import smile.regression.OLS;
import smile.regression.Regression;
import smile.regression.RidgeRegression;

/** OLS */
public class RidgeRegressionHook extends RegressionLinearHook<OLS> {

  public RidgeRegressionHook(
      String mode,
      Pipeline pipeline,
      Map<String, Schema> schemas,
      Map<String, Step> steps,
      Map<String, String> modesteps,
      Step mlStep,
      MLModelTrainer blackBoxTransformer) {
    super(mode, pipeline, schemas, steps, modesteps, mlStep, blackBoxTransformer);
  }

  @Override
  protected MutableQuadruple<Schema, Iterable<Row>, String, Regression<double[]>> trainTemplate(
      Attribute[] attributes, double[][] x, double[] y) {

    Double lambda = (Double) this.mlStep.config.get(StepConfig.ML_LAMBDA);

    Preconditions.checkNotNull(
        lambda,
        "Please choose a lambda regularization factor. Large lambda means more shrinkage.\n"
            + "     *               Choosing an appropriate value of lambda is important/ "
            + StepConfig.ML_LAMBDA);

    RidgeRegression model = new RidgeRegression(x, y, lambda.doubleValue());

    Schema schema =
        Schema.builder()
            .addNullableField("residual sum of squares", Schema.FieldType.DOUBLE)
            .addNullableField("shrinkage", Schema.FieldType.DOUBLE)
            .addNullableField("Ftest", Schema.FieldType.DOUBLE)
            .addNullableField("Rsquared", Schema.FieldType.DOUBLE)
            .addNullableField("adjustedRsquared", Schema.FieldType.DOUBLE)
            .addNullableField("df", Schema.FieldType.INT32)
            .addNullableField("residual SumOfSquares", Schema.FieldType.DOUBLE)
            .addNullableField("residual Std Error", Schema.FieldType.DOUBLE)
            .addNullableField("residuals Min", Schema.FieldType.DOUBLE)
            .addNullableField("residuals 1Q", Schema.FieldType.DOUBLE)
            .addNullableField("residuals Median", Schema.FieldType.DOUBLE)
            .addNullableField("residuals 3Q", Schema.FieldType.DOUBLE)
            .addNullableField("residuals Max", Schema.FieldType.DOUBLE)
            .build();

    List<Row> a = new ArrayList<>();
    double[] r = model.residuals().clone();

    a.add(
        Row.withSchema(schema)
            .addValue(model.RSS())
            .addValue(model.shrinkage())
            .addValue(model.ftest())
            .addValue(model.RSquared())
            .addValue(model.adjustedRSquared())
            .addValue(model.df())
            .addValue(model.error())
            .addValue(Math.min(r))
            .addValue(Math.q1(r))
            .addValue(Math.median(r))
            .addValue(Math.q3(r))
            .addValue(Math.max(r))
            .build());
    return MutableQuadruple.of(
        schema, a, "Training completed; Results : " + model.toString(), model);
  }
}
