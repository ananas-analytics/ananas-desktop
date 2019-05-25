package org.ananas.runner.model.steps.ml.featureselection;

import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.model.steps.ml.MLModelTrainer;
import org.apache.beam.repackaged.beam_sdks_java_core.org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;
import smile.classification.ClassifierTrainer;
import smile.data.Attribute;
import smile.feature.GAFeatureSelection;
import smile.gap.BitString;
import smile.regression.RegressionTrainer;
import smile.validation.Accuracy;
import smile.validation.ClassificationMeasure;
import smile.validation.RSS;
import smile.validation.RegressionMeasure;

/** Genetic Algorithm Based Feature Selection */
public class GAFeatureSelectionHook extends FeatureSelectionHook {

  public GAFeatureSelectionHook(
      String mode,
      Pipeline pipeline,
      Map<String, Schema> schemas,
      Map<String, Step> steps,
      Map<String, String> modesteps,
      Step mlStep,
      MLModelTrainer blackBoxTransformer) {
    super(mode, pipeline, schemas, steps, modesteps, mlStep, blackBoxTransformer);
  }

  protected MutableTriple<Schema, Iterable<Row>, String> onRegression(
      Class clazz, Attribute[] attributes, double[][] x, double[] y) {

    RegressionTrainer<double[]> trainer = null;

    try {
      trainer = (RegressionTrainer<double[]>) clazz.getDeclaredConstructor().newInstance();

    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("OOps can't create model ");
    }

    // KNN.Trainer trainer = new KNN.Trainer(100);

    RegressionMeasure measure = new RSS();

    // Use default settings for Genetic Algorithm.
    GAFeatureSelection selector = new GAFeatureSelection();

    // The population size is 50, and 20 generation evolution.
    // Each returned BitString contains the selected feature subset and its fitness.
    BitString[] bitStrings = selector.learn(50, 3, trainer, measure, x, y, 5);

    return getSchemaIterableStringMutableTriple(bitStrings);
  }

  private MutableTriple<Schema, Iterable<Row>, String> getSchemaIterableStringMutableTriple(
      BitString[] bitStrings) {
    Schema schema =
        Schema.builder()
            .addNullableField("features", Schema.FieldType.STRING)
            .addNullableField("fitness", Schema.FieldType.STRING)
            .build();

    List<Row> a = new ArrayList<>();
    for (int i = 0; i < bitStrings.length; i++) {
      a.add(
          Row.withSchema(schema)
              .addValue(convert(bitStrings[i].bits()))
              .addValue(String.valueOf((100 * bitStrings[i].fitness())) + "%")
              .build());
    }

    return MutableTriple.of(schema, a, "Feature Selection completed.");
  }

  protected MutableTriple<Schema, Iterable<Row>, String> onClassifier(
      Class clazz, Attribute[] attributes, double[][] x, int[] y) {

    ClassifierTrainer<double[]> trainer = null;

    try {
      trainer = (ClassifierTrainer<double[]>) clazz.getDeclaredConstructor().newInstance();

    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("OOps can't create model ");
    }

    // KNN.Trainer trainer = new KNN.Trainer(100);

    ClassificationMeasure measure = new Accuracy();

    // Use default settings for Genetic Algorithm.
    GAFeatureSelection selector = new GAFeatureSelection();

    // The population size is 50, and 20 generation evolution.
    // Each returned BitString contains the selected feature subset and its fitness.
    BitString[] bitStrings = selector.learn(50, 3, trainer, measure, x, y, 5);

    return getSchemaIterableStringMutableTriple(bitStrings);
  }

  private String convert(int[] bits) {
    Schema schema = this.previousSchemas.get(this.mode);
    List<String> fieldNames = new ArrayList<>();
    for (int i = 0; i < bits.length; i++) {
      if (bits[i] == 1) {
        Schema.Field f = schema.getField(i);
        fieldNames.add(f.getName());
      }
    }
    return Joiner.on(" ").join(fieldNames);
  }
}
