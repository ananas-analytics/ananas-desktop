package org.ananas.runner.legacy.steps.ml.classifier;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.legacy.core.StepConfig;
import org.ananas.runner.legacy.steps.ml.MLModelTrainer;
import org.ananas.runner.legacy.steps.ml.classifier.common.ClassificationHook;
import org.ananas.runner.misc.MutableQuadruple;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;
import smile.classification.Classifier;
import smile.classification.RDA;
import smile.data.Attribute;

/** AdaBoost classifier */
public class RDAHook extends ClassificationHook {

  public RDAHook(
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
  protected MutableQuadruple<Schema, Iterable<Row>, String, Classifier<double[]>> trainTemplate(
      Attribute[] attributes, double[][] x, int[] y) {

    Double alpha = (Double) this.mlStep.config.get(StepConfig.ML_ALPHA);

    Preconditions.checkNotNull(
        alpha,
        "Please choose an alpha regularization factor in [0, 1] which allows a continuum of models/ "
            + StepConfig.ML_ALPHA);

    RDA model = new RDA(x, y, alpha);

    Schema schema =
        Schema.builder().addNullableField("description", Schema.FieldType.STRING).build();

    List<Row> a = new ArrayList<>();
    a.add(Row.withSchema(schema).addValue(model.toString()).build());

    return MutableQuadruple.of(schema, a, "Training completed", model);
  }
}
