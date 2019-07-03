package org.ananas.runner.legacy.steps.ml.classifier;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.legacy.core.StepConfig;
import org.ananas.runner.legacy.steps.ml.MLModelTrainer;
import org.ananas.runner.legacy.steps.ml.classifier.common.ClassificationHook;
import org.ananas.runner.legacy.steps.ml.distance.DistanceFactory;
import org.ananas.runner.misc.MutableQuadruple;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;
import smile.classification.Classifier;
import smile.classification.KNN;
import smile.data.Attribute;
import smile.math.distance.Distance;

/** AdaBoost classifier */
public class KNNHook extends ClassificationHook {

  public KNNHook(
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
    // Integer k = (Integer) this.mlStep.config.getOrDefault(StepConfig.ML_K, 10);
    String distanceLabel = (String) this.mlStep.config.get(StepConfig.ML_DISTANCE);
    Preconditions.checkNotNull(
        distanceLabel, "Please choose a distance  / " + StepConfig.ML_DISTANCE);

    Distance distance = DistanceFactory.of(distanceLabel);

    KNN model = new KNN(x, y, distance);

    Schema schema = Schema.builder().addNullableField("count", Schema.FieldType.INT32).build();

    List<Row> a = new ArrayList<>();

    a.add(Row.withSchema(schema).addValue(y.length).build());

    return MutableQuadruple.of(schema, a, "Training completed", model);
  }
}
