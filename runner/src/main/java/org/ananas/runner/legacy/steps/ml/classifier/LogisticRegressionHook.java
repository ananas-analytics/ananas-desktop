package org.ananas.runner.legacy.steps.ml.classifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.legacy.steps.ml.MLModelTrainer;
import org.ananas.runner.legacy.steps.ml.classifier.common.ClassificationHook;
import org.ananas.runner.misc.MutableQuadruple;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;
import smile.classification.Classifier;
import smile.classification.LogisticRegression;
import smile.data.Attribute;

/** LogisticRegression classifier */
public class LogisticRegressionHook extends ClassificationHook {

  public LogisticRegressionHook(
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

    LogisticRegression model = new LogisticRegression(x, y);

    Schema schema =
        Schema.builder().addNullableField("loglikelihood", Schema.FieldType.DOUBLE).build();

    List<Row> a = new ArrayList<>();
    a.add(Row.withSchema(schema).addValue(model.loglikelihood()).build());

    return MutableQuadruple.of(schema, a, "Training completed", model);
  }
}
