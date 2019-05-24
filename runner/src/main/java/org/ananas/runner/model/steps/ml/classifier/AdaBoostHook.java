package org.ananas.runner.model.steps.ml.classifier;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.ananas.runner.misc.MutableQuadruple;
import org.ananas.runner.model.core.Step;
import org.ananas.runner.model.core.StepConfig;
import org.ananas.runner.model.steps.ml.MLModelTrainer;
import org.ananas.runner.model.steps.ml.classifier.common.ClassificationHook;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;
import smile.classification.AdaBoost;
import smile.classification.Classifier;
import smile.data.Attribute;

/** AdaBoost classifier */
public class AdaBoostHook extends ClassificationHook {

  public AdaBoostHook(
      String mode,
      Pipeline pipeline,
      Map<String, Schema> schemas,
      Map<String, Step> steps,
      Map<String, String> modesteps,
      Step mlStep,
      MLModelTrainer blackBoxTransformer) {
    super(mode, pipeline, schemas, steps, modesteps, mlStep, blackBoxTransformer);
  }

  /*@Override
  protected void check(AdaBoost model) {
  	if (model.getTrees() == null || model.getTrees().length == 0) {
  		throw new RuntimeException("Oops. Something wrong with your train model. It has 0 tree");
  	}
  }*/

  @Override
  protected MutableQuadruple<Schema, Iterable<Row>, String, Classifier<double[]>> trainTemplate(
      Attribute[] attributes, double[][] x, int[] y) {
    Integer ntrees = (Integer) this.mlStep.config.get(StepConfig.ML_TREES);
    Preconditions.checkNotNull(
        ntrees, "Please choose the number of trees / " + StepConfig.ML_TREES);
    // Integer maxNodes = (Integer) this.mlStep.config.getOrDefault(StepConfig.ML_MAX_NODES, 4);

    AdaBoost forest = new AdaBoost(attributes, x, y, ntrees);

    Schema schema =
        Schema.builder()
            .addNullableField("dot", Schema.FieldType.STRING)
            .addNullableField("maxDepth", Schema.FieldType.INT32)
            .build();

    List<Row> a = new ArrayList<>();
    for (int i = 0; i < forest.getTrees().length; i++) {
      a.add(
          Row.withSchema(schema)
              .addValue(forest.getTrees()[i].dot())
              .addValue(forest.getTrees()[i].maxDepth())
              .build());
    }

    return MutableQuadruple.of(schema, a, "Training completed", forest);
  }
}
