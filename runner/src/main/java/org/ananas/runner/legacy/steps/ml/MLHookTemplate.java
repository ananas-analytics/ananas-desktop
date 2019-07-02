package org.ananas.runner.legacy.steps.ml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.ananas.runner.kernel.AbstractStepRunner;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.kernel.pipeline.PipelineHook;
import org.ananas.runner.legacy.core.StepConfig;
import org.ananas.runner.legacy.steps.ml.ft.*;
import org.ananas.runner.misc.SerializationUtils;
import org.ananas.runner.steprunner.files.FileLoader;
import org.ananas.runner.steprunner.files.utils.HomeManager;
import org.ananas.runner.steprunner.files.utils.StepFileConfigToUrl;
import org.apache.beam.repackaged.beam_sdks_java_core.org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.Row;
import smile.data.Attribute;
import smile.data.AttributeDataset;
import smile.data.NominalAttribute;
import smile.data.NumericAttribute;
import smile.data.parser.DelimitedTextParser;
import smile.feature.FeatureTransform;

public abstract class MLHookTemplate implements PipelineHook {

  protected String mode;
  protected Pipeline pipeline;
  protected Map<String, Schema> previousSchemas;
  protected Map<String, Step> steps;
  protected Step mlStep;
  protected AbstractStepRunner stepRunner;

  public static String getSerializedModelPath(String stepId) {
    return HomeManager.getHomeFilePath("model_" + stepId + ".ananas");
  }

  protected String getSerializedModelPath() {
    return getSerializedModelPath(this.mlStep.id);
  }

  public static Schema.Builder getSchemaBuilder(Schema schema) {
    Schema.Builder schemaBuilder = Schema.builder();
    for (Schema.Field f : schema.getFields()) {
      schemaBuilder.addNullableField(f.getName(), Schema.FieldType.DOUBLE);
    }
    return schemaBuilder;
  }

  protected Predictor deserializeModel() {
    Predictor model = (Predictor) SerializationUtils.deserialize(getSerializedModelPath(), true);
    model.check();
    return model;
  }

  protected Function<double[], Row> datasetDoubles2Row(Schema schema) {
    return x -> {
      Row.Builder builder = Row.withSchema(schema);
      for (double d : x) {
        builder.addValue(Double.valueOf(d));
      }
      return builder.build();
    };
  }

  public enum MLMode {
    PREDICT,
    TRAIN,
    TEST
  }

  public MLHookTemplate(
      String mode,
      Pipeline pipeline,
      Map<String, Schema> schemas,
      Map<String, Step> steps,
      Map<String, String> modeSteps,
      Step mlStep,
      AbstractStepRunner blackBoxTransformer) {

    this.mode = mode;
    this.pipeline = pipeline;
    this.previousSchemas = new HashMap<>();
    this.steps = new HashMap<>();
    for (String m : modeSteps.keySet()) {
      Step step = steps.get(modeSteps.get(m));
      if (step != null) {
        this.steps.put(m, step);
        this.previousSchemas.put(m, schemas.get(step.id));
      }
    }
    this.mlStep = mlStep;
    this.stepRunner = blackBoxTransformer;
  }

  @Override
  public void run() {
    MutableTriple<Schema, Iterable<Row>, String> rows = MLRun();
    Create.Values<org.apache.beam.sdk.values.Row> MLRows = Create.of(rows.getMiddle());
    PCollection<Row> pCollection = PBegin.in(this.pipeline).apply(MLRows);
    pCollection.setRowSchema(rows.getLeft());
    this.stepRunner.setOutput(pCollection);
    this.stepRunner.setReader();
    this.stepRunner.setOutputMessage(rows.getRight());
  }

  protected abstract MutableTriple<Schema, Iterable<Row>, String> MLRun();

  protected AttributeDataset extractDataset(String mode, boolean forceNominal) {
    if (this.steps.get(mode) == null) {
      throw new RuntimeException(
          String.format("No step found for this action. Choose a %s step", mode));
    }
    String url =
        StepFileConfigToUrl.url(this.steps.get(mode).config, FileLoader.SupportedFormat.CSV);
    this.steps.get(mode).config.put(StepConfig.PATH, url);
    this.steps.get(mode).config.put(StepConfig.IS_HEADER, true);

    DelimitedTextParser parser = new DelimitedTextParser();
    parser.setColumnNames(true);
    parser.setDelimiter(",");

    String yFieldName = (String) this.mlStep.config.get(StepConfig.ML_Y_COLUMN_NAME);
    Schema schema = this.previousSchemas.get(mode);

    parser.setMissingValuePlaceholder("");
    ArrayList<Attribute> attributes = getAttributes(yFieldName, schema, forceNominal);
    setResponseAttribute(parser, yFieldName, schema, forceNominal);
    AttributeDataset dataset;
    try {
      Attribute[] arr = new Attribute[attributes.size()];
      arr = attributes.toArray(arr);
      dataset = parser.parse(arr, url);
    } catch (Exception e) {
      throw new RuntimeException("Unable to read file ", e);
    }
    return dataset;
  }

  private void setResponseAttribute(
      DelimitedTextParser parser, String yFieldName, Schema schema, boolean forceNominal) {
    for (int j = 0; j < schema.getFields().size(); j++) {
      if (yFieldName != null && schema.getField(j).getName().equalsIgnoreCase(yFieldName)) {
        Attribute a = attribute(schema.getField(j), forceNominal);
        parser.setResponseIndex(a, j);
      }
    }
  }

  private static ArrayList<Attribute> getAttributes(
      String yFieldName, Schema schema, boolean forceNominal) {
    ArrayList<Attribute> attributes = new ArrayList<>();
    for (int j = 0; j < schema.getFields().size(); j++) {
      Attribute a = attribute(schema.getField(j), forceNominal);
      if (yFieldName == null || !schema.getField(j).getName().equalsIgnoreCase(yFieldName)) {
        attributes.add(a);
      }
    }
    return attributes;
  }

  public static Attribute attribute(Schema.Field f, boolean forceNominal) {
    if (f.getType().getTypeName().isStringType()) {
      return new NominalAttribute(f.getName());
    } else {
      return forceNominal ? new NominalAttribute(f.getName()) : new NumericAttribute(f.getName());
    }
  }

  protected FeatureTransform getTransformTf() {
    FeatureTransform tf = null;
    String ftStr = (String) this.mlStep.config.get(StepConfig.ML_FT);
    if (ftStr != null) {
      switch (ftStr) {
        case "standardizer":
          tf = new Standardizer();
          break;
        case "normalizer":
          tf = new Normalizer();
          break;
        case "robuststandardizer":
          tf = new RobustStandardizer();
          break;
        case "scaler":
          tf = new Scaler();
          break;
        case "maxabsscaler":
          tf = new MaxAbsScaler();
          break;
        default:
          throw new RuntimeException("Unknown feature transform " + ftStr);
      }
    }
    return tf;
  }

  protected double[][] transform(double[][] x, FeatureTransform tf, Attribute[] attributes) {
    if (tf != null) {
      tf.learn(attributes, x);
      return tf.transform(x);
    }
    return x;
  }
}
