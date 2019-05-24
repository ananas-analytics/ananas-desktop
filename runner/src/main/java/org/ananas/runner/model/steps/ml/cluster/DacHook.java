package org.ananas.runner.model.steps.ml.cluster;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.ananas.runner.model.core.Step;
import org.ananas.runner.model.core.StepConfig;
import org.ananas.runner.model.steps.ml.MLHookTemplate;
import org.ananas.runner.model.steps.ml.MLModelTrainer;
import org.apache.beam.repackaged.beam_sdks_java_core.org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;
import smile.clustering.DeterministicAnnealing;
import smile.data.Attribute;
import smile.data.AttributeDataset;
import smile.feature.FeatureTransform;

public class DacHook extends ClusteringHook {
  public DacHook(
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
    Integer kMaxCluster = (Integer) this.mlStep.config.get(StepConfig.ML_CLUSTER_NUM);

    Preconditions.checkNotNull(
        kMaxCluster, "Please choose the max number of clusters / " + StepConfig.ML_CLUSTER_NUM);

    FeatureTransform tf = getTransformTf();

    Attribute[] attributes = dataset.attributes();
    double[][] xt = transform(dataset.x(), tf, attributes);

    DeterministicAnnealing k = new DeterministicAnnealing(xt, kMaxCluster);

    double[][] centroids = k.centroids();

    serialize(k, tf);

    Schema schema = MLHookTemplate.getSchemaBuilder(this.previousSchemas.get(this.mode)).build();

    List<Row> a =
        Arrays.asList(centroids)
            .stream()
            .map(datasetDoubles2Row(schema))
            .collect(Collectors.toList());
    return MutableTriple.of(schema, a, k.toString());
  }
}
