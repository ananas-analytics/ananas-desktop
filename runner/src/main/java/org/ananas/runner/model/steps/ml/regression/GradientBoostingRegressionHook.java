package org.ananas.runner.model.steps.ml.regression;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.misc.MutableQuadruple;
import org.ananas.runner.model.core.StepConfig;
import org.ananas.runner.model.steps.ml.MLModelTrainer;
import org.ananas.runner.model.steps.ml.regression.common.RegressionLinearHook;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;
import smile.data.Attribute;
import smile.regression.GradientTreeBoost;
import smile.regression.OLS;
import smile.regression.Regression;

/** OLS */
public class GradientBoostingRegressionHook extends RegressionLinearHook<OLS> {

  public GradientBoostingRegressionHook(
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

    Integer ntrees = (Integer) this.mlStep.config.get(StepConfig.ML_TREES);
    Preconditions.checkNotNull(
        ntrees, "Please choose the number of trees / " + StepConfig.ML_TREES);

    GradientTreeBoost model = new GradientTreeBoost(x, y, ntrees.intValue());

    Schema schema =
        Schema.builder()
            .addNullableField("SamplingRate", Schema.FieldType.DOUBLE)
            .addNullableField("LossFunction", Schema.FieldType.STRING)
            .addNullableField("MaxNodes", Schema.FieldType.INT32)
            .build();

    List<Row> a = new ArrayList<>();

    a.add(
        Row.withSchema(schema)
            .addValue(model.getSamplingRate())
            .addValue(model.getLossFunction().name())
            .addValue(model.getmaxNodes())
            .build());
    return MutableQuadruple.of(
        schema, a, "Training completed; Results : " + model.toString(), model);
  }
}
