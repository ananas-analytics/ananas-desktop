package org.ananas.runner.model.steps.ml.featureselection;

import java.util.Map;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.model.steps.ml.MLHookTemplate;
import org.ananas.runner.model.steps.ml.MLModelTrainer;
import org.apache.beam.repackaged.beam_sdks_java_core.org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;
import smile.data.Attribute;
import smile.data.AttributeDataset;
import smile.feature.FeatureTransform;
import smile.feature.Standardizer;

public abstract class FeatureSelectionHook<T extends Standardizer> extends MLHookTemplate {

  public FeatureSelectionHook(
      String mode,
      Pipeline pipeline,
      Map<String, Schema> schemas,
      Map<String, Step> steps,
      Map<String, String> modesteps,
      Step mlStep,
      MLModelTrainer blackBoxTransformer) {
    super(mode, pipeline, schemas, steps, modesteps, mlStep, blackBoxTransformer);
  }

  /**
   * Select k feature and serialize model
   *
   * @return the list of features set with their fitness
   */
  protected MutableTriple<Schema, Iterable<Row>, String> select(AttributeDataset dataset) {

    double[][] x = dataset.toArray(new double[dataset.size()][]);

    FeatureTransform tf = getTransformTf();

    Attribute[] attributes = dataset.attributes();
    double[][] xt = transform(x, tf, attributes);

    String algo = (String) this.mlStep.config.get("algo_selected");
    Preconditions.checkNotEmpty(
        algo, "Select the underlying model from which you want to select features. ");

    Class clazz = MLModelTrainer.ALGOS_CLASSIFIER.get(algo);
    if (clazz != null) {
      int[] y = dataset.toArray(new int[dataset.size()]);
      return onClassifier(clazz, attributes, xt, y);
    }
    clazz = MLModelTrainer.ALGOS_REGRESSION.get(algo);
    if (clazz != null) {
      double[] y = dataset.toArray(new double[dataset.size()]);
      return onRegression(clazz, attributes, xt, y);
    }

    throw new RuntimeException(
        "Sorry we can't run a genetic algorithm feautre selection on such algorithm");
  }

  protected abstract MutableTriple<Schema, Iterable<Row>, String> onRegression(
      Class modelTrainerClass, Attribute[] attributes, double[][] x, double[] y);

  protected abstract MutableTriple<Schema, Iterable<Row>, String> onClassifier(
      Class modelTrainerClass, Attribute[] attributes, double[][] x, int[] y);

  @Override
  public MutableTriple<Schema, Iterable<Row>, String> MLRun() {
    AttributeDataset dataset = extractDataset(this.mode, true);
    return select(dataset);
  }
}
