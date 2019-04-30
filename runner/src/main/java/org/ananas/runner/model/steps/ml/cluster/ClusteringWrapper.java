package org.ananas.runner.model.steps.ml.cluster;


import org.ananas.runner.model.steps.ml.Predictor;
import org.apache.beam.sdk.schemas.Schema;
import smile.clustering.PartitionClustering;
import smile.feature.FeatureTransform;

import java.io.Serializable;

public class ClusteringWrapper<T extends PartitionClustering<double[]>> implements Predictor<Integer>, Serializable {

	private static final long serialVersionUID = 7423719663600179750L;
	private T subject;
	private FeatureTransform ft;

	public ClusteringWrapper(T subject, FeatureTransform ft) {
		this.ft = ft;
		this.subject = subject;
	}

	public static <U extends PartitionClustering<double[]>> ClusteringWrapper of(U s, FeatureTransform ft) {
		return new ClusteringWrapper<>(s, ft);
	}

	public T getSubject() {
		return this.subject;
	}

	@Override
	public Integer predict(double[] x) {
		if (this.ft != null) {
			return this.subject.predict(this.ft.transform(x));
		}
		return this.subject.predict(x);
	}

	@Override
	public Schema.FieldType getPredictedType() {
		return Schema.FieldType.INT32;
	}
}
