package org.ananas.runner.model.steps.ml.cluster;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.model.core.StepConfig;
import org.ananas.runner.model.steps.ml.MLHookTemplate;
import org.ananas.runner.model.steps.ml.MLModelTrainer;
import org.ananas.runner.model.steps.ml.distance.DistanceFactory;
import org.apache.beam.repackaged.beam_sdks_java_core.org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;
import smile.clustering.CLARANS;
import smile.data.Attribute;
import smile.data.AttributeDataset;
import smile.feature.FeatureTransform;
import smile.math.distance.Distance;

public class ClaransHook extends ClusteringHook {
  public ClaransHook(
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

    String distanceLabel = (String) this.mlStep.config.get(StepConfig.ML_DISTANCE);
    Preconditions.checkNotNull(
        distanceLabel, "Please choose a distance  / " + StepConfig.ML_DISTANCE);

    Distance distance = DistanceFactory.of(distanceLabel);

    Preconditions.checkNotNull(
        kCluster, "Please choose the number of clusters / " + StepConfig.ML_CLUSTER_NUM);

    FeatureTransform tf = getTransformTf();

    Attribute[] attributes = dataset.attributes();
    double[][] xt = transform(dataset.x(), tf, attributes);

    CLARANS<double[]> k = new CLARANS<double[]>(xt, distance, kCluster);

    double[][] centroids = k.medoids();

    serialize(k, tf);

    Schema schema = MLHookTemplate.getSchemaBuilder(this.previousSchemas.get(this.mode)).build();

    List<Row> a =
        Arrays.asList(centroids).stream()
            .map(datasetDoubles2Row(schema))
            .collect(Collectors.toList());
    return MutableTriple.of(schema, a, k.toString());
  }
}
