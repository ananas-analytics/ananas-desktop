package org.ananas.runner.legacy.steps.ml.cluster;

import java.util.Map;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.misc.SerializationUtils;
import org.ananas.runner.legacy.steps.ml.MLHookTemplate;
import org.ananas.runner.legacy.steps.ml.MLModelTrainer;
import org.apache.beam.repackaged.beam_sdks_java_core.org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;
import smile.clustering.PartitionClustering;
import smile.data.AttributeDataset;
import smile.feature.FeatureTransform;

public abstract class ClusteringHook extends MLHookTemplate {

  public ClusteringHook(
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
    AttributeDataset dataset = extractDataset(this.mode, false);

    switch (this.mode) {
      case "train":
        return train(dataset);
      case "test":
        throw new IllegalStateException(
            "Cannot test an unsupervised algorithm. You can predict with your trained legacy directly.");
      default:
        throw new IllegalStateException(
            "Oops. Unknown mode : "
                + this.mode
                + " . Choose one of the following modes : "
                + MLMode.values());
    }
  }

  /**
   * Run train dataset , train legacy with supervisation , serialize legacy and eventually return
   * clusters.
   *
   * @param dataset
   * @return the list of clusters
   */
  protected abstract MutableTriple<Schema, Iterable<Row>, String> train(AttributeDataset dataset);

  protected void serialize(PartitionClustering<double[]> k, FeatureTransform ft) {
    SerializationUtils.serialize(ClusteringWrapper.of(k, ft), getSerializedModelPath(), true);
  }
}
