package org.ananas.runner.model.steps.ml.classifier;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.misc.MutableQuadruple;
import org.ananas.runner.model.core.StepConfig;
import org.ananas.runner.model.steps.ml.MLModelTrainer;
import org.ananas.runner.model.steps.ml.classifier.common.ClassificationHook;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;
import smile.classification.Classifier;
import smile.classification.RandomForest;
import smile.data.Attribute;

/** Random forest classifier */
public class RandomForestHook extends ClassificationHook {

  public RandomForestHook(
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

    Integer ntrees = (Integer) this.mlStep.config.get(StepConfig.ML_TREES);
    Preconditions.checkNotNull(
        ntrees, "Please choose the number of trees / " + StepConfig.ML_TREES);

    RandomForest model = new RandomForest(attributes, x, y, ntrees);

    Schema schema =
        Schema.builder()
            .addNullableField("dot", Schema.FieldType.STRING)
            .addNullableField("maxDepth", Schema.FieldType.INT32)
            .build();

    List<Row> a = new ArrayList<>();
    for (int i = 0; i < model.getTrees().length; i++) {
      a.add(
          Row.withSchema(schema)
              .addValue(model.getTrees()[i].dot())
              .addValue(model.getTrees()[i].maxDepth())
              .build());
    }
    return MutableQuadruple.of(schema, a, "Training completed", model);
  }
}
