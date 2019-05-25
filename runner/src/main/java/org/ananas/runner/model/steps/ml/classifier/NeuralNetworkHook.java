package org.ananas.runner.model.steps.ml.classifier;

import java.util.ArrayList;
import java.util.Arrays;
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
import smile.classification.NeuralNetwork;
import smile.data.Attribute;

/** Neural network classifier */
public class NeuralNetworkHook extends ClassificationHook {

  public NeuralNetworkHook(
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
    List<Integer> numUnitsList =
        (List<Integer>) this.mlStep.config.getOrDefault(StepConfig.ML_NN_UNITS, Arrays.asList(10));
    int[] numUnits = new int[numUnitsList.size()];
    for (int i = 0; i < numUnitsList.size(); i++) {
      numUnits[i] = numUnitsList.get(i);
    }

    String activationFunction =
        (String)
            this.mlStep.config.getOrDefault(
                NeuralNetwork.ActivationFunction.valueOf(StepConfig.ML_NN_ACTIVATION_FUNCTION),
                NeuralNetwork.ActivationFunction.LOGISTIC_SIGMOID.name());

    NeuralNetwork model =
        new NeuralNetwork(
            NeuralNetwork.ErrorFunction.LEAST_MEAN_SQUARES,
            NeuralNetwork.ActivationFunction.valueOf(activationFunction.toUpperCase()),
            numUnits);

    Schema schema =
        Schema.builder()
            .addNullableField("LearningRate", Schema.FieldType.DOUBLE)
            .addNullableField("Momentum", Schema.FieldType.DOUBLE)
            .addNullableField("WeightDecay", Schema.FieldType.DOUBLE)
            .addNullableField("description", Schema.FieldType.STRING)
            .build();

    List<Row> a = new ArrayList<>();
    a.add(
        Row.withSchema(schema)
            .addValue(model.getMomentum())
            .addValue(model.getWeightDecay())
            .addValue(model.getLearningRate())
            .addValue(model.toString())
            .build());

    return MutableQuadruple.of(schema, a, "Training completed", model);
  }
}
