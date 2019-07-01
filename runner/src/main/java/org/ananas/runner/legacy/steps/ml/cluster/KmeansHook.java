package org.ananas.runner.legacy.steps.ml.cluster;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.legacy.core.StepConfig;
import org.ananas.runner.legacy.steps.ml.MLHookTemplate;
import org.ananas.runner.legacy.steps.ml.MLModelTrainer;
import org.apache.beam.repackaged.beam_sdks_java_core.org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;
import smile.clustering.KMeans;
import smile.data.Attribute;
import smile.data.AttributeDataset;
import smile.feature.FeatureTransform;

public class KmeansHook extends ClusteringHook {
  public KmeansHook(
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
  protected MutableTriple<Schema, Iterable<Row>, String> train(AttributeDataset dataset) {
    Integer kCluster = (Integer) this.mlStep.config.get(StepConfig.ML_CLUSTER_NUM);
    Preconditions.checkNotNull(
        kCluster, "Please choose the number of clusters / " + StepConfig.ML_CLUSTER_NUM);

    FeatureTransform tf = getTransformTf();

    Attribute[] attributes = dataset.attributes();
    double[][] xt = transform(dataset.x(), tf, attributes);

    KMeans k = KMeans.lloyd(xt, kCluster);

    double[][] centroids = k.centroids();

    serialize(k, tf);

    Schema schema = MLHookTemplate.getSchemaBuilder(this.previousSchemas.get(this.mode)).build();

    List<Row> a =
        Arrays.asList(centroids).stream()
            .map(datasetDoubles2Row(schema))
            .collect(Collectors.toList());
    return MutableTriple.of(schema, a, k.toString());
  }
}
