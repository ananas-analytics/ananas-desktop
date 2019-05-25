package org.ananas.runner.model.steps.ml.classifier.common;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.misc.MutableQuadruple;
import org.ananas.runner.misc.SerializationUtils;
import org.ananas.runner.model.steps.ml.MLHookTemplate;
import org.ananas.runner.model.steps.ml.MLModelTrainer;
import org.ananas.runner.model.steps.ml.Predictor;
import org.apache.beam.repackaged.beam_sdks_java_core.org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;
import smile.classification.Classifier;
import smile.data.Attribute;
import smile.data.AttributeDataset;
import smile.feature.FeatureTransform;

public abstract class ClassificationHook extends MLHookTemplate {

  public ClassificationHook(
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
  public MutableTriple<Schema, Iterable<Row>, String> MLRun() {

    // https://github.com/haifengl/smile/wiki/Tutorial:-A-Gentle-Introduction-to-Smile
    AttributeDataset dataset = extractDataset(this.mode, true);

    switch (this.mode) {
      case "train":
        return train(dataset);
      case "test":
        return test(dataset);
      default:
        throw new IllegalStateException(
            "Oops. Unknown mode : "
                + this.mode
                + " . Choose one of the following modes : "
                + MLMode.values());
    }
  }

  protected MutableTriple<Schema, Iterable<Row>, String> test(AttributeDataset dataset) {
    Predictor<Integer> model = deserializeModel();
    double[][] x = dataset.toArray(new double[dataset.size()][]);
    int[] y = dataset.toArray(new int[dataset.size()]);

    int error = 0;
    int count = x.length;
    long totalPredic = 0;

    for (int i = 0; i < x.length; i++) {
      Integer predict = model.predict(x[i]);
      if (predict != null && predict.intValue() != y[i]) {
        error++;
      }
      totalPredic += predict;
    }

    double errorRate = error / x.length;
    double mean = totalPredic / x.length;

    Schema schema =
        Schema.builder()
            .addNullableField("count", Schema.FieldType.INT32)
            .addNullableField("mean", Schema.FieldType.DOUBLE)
            .addNullableField("errorRate%", Schema.FieldType.DOUBLE)
            .build();

    List<Row> a =
        Arrays.asList(
            Row.withSchema(schema).addValue(count).addValue(mean).addValue(errorRate).build());

    return MutableTriple.of(schema, a, "Test completed");
  }

  /**
   * Train and serialize model
   *
   * @param dataset
   * @return the list of clusters
   */
  protected MutableTriple<Schema, Iterable<Row>, String> train(AttributeDataset dataset) {

    double[][] x = dataset.toArray(new double[dataset.size()][]);
    int[] y = dataset.toArray(new int[dataset.size()]);

    FeatureTransform tf = getTransformTf();

    Attribute[] attributes = dataset.attributes();
    double[][] xt = transform(x, tf, attributes);

    MutableQuadruple<Schema, Iterable<Row>, String, Classifier<double[]>> tuple =
        trainTemplate(attributes, xt, y);

    serialize(tuple.fourth, tf);
    return MutableTriple.of(tuple.first, tuple.second, tuple.third);
  }

  /**
   * Train and serialize model
   *
   * @return the list of clusters
   */
  protected abstract MutableQuadruple<Schema, Iterable<Row>, String, Classifier<double[]>>
      trainTemplate(Attribute[] attributes, double[][] x, int[] y);

  protected void serialize(Classifier<double[]> model, FeatureTransform ft) {
    SerializationUtils.serialize(ClassifierWrapper.of(model, ft), getSerializedModelPath(), true);
  }
}
