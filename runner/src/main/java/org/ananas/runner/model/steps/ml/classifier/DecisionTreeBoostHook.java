package org.ananas.runner.model.steps.ml.classifier;

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
import smile.classification.Classifier;
import smile.classification.DecisionTree;
import smile.data.Attribute;

/** AdaBoost classifier */
public class DecisionTreeBoostHook extends ClassificationHook {

  public DecisionTreeBoostHook(
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
    Integer maxNodes = (Integer) this.mlStep.config.getOrDefault(StepConfig.ML_MAX_NODES, 4);
    DecisionTree model = new DecisionTree(attributes, x, y, maxNodes);

    Schema schema =
        Schema.builder()
            .addNullableField("dot", Schema.FieldType.STRING)
            .addNullableField("maxDepth", Schema.FieldType.INT32)
            .build();

    List<Row> a = new ArrayList<>();
    a.add(Row.withSchema(schema).addValue(model.dot()).addValue(model.maxDepth()).build());

    return MutableQuadruple.of(schema, a, "Training completed", model);
  }
}
