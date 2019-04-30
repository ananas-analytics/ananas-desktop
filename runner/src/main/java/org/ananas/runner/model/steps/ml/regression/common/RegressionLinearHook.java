package org.ananas.runner.model.steps.ml.regression.common;

import org.ananas.runner.misc.SerializationUtils;
import org.ananas.runner.model.core.Step;
import org.ananas.runner.model.steps.ml.MLHookTemplate;
import org.ananas.runner.model.steps.ml.MLModelTrainer;
import org.ananas.runner.model.steps.ml.Predictor;
import org.ananas.runner.utils.MutableQuadruple;
import org.apache.beam.repackaged.beam_sdks_java_core.org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;
import smile.data.Attribute;
import smile.data.AttributeDataset;
import smile.feature.FeatureTransform;
import smile.regression.Regression;
import smile.validation.MSE;
import smile.validation.RegressionMeasure;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class RegressionLinearHook<T extends Regression<double[]>> extends MLHookTemplate {


	public RegressionLinearHook(String mode,
								Pipeline pipeline,
								Map<String, Schema> schemas,
								Map<String, Step> steps,
								Map<String, String> modesteps,
								Step mlStep, MLModelTrainer blackBoxTransformer) {
		super(mode, pipeline, schemas, steps, modesteps, mlStep, blackBoxTransformer);
	}

	@Override
	public MutableTriple<Schema, Iterable<Row>, String> MLRun() {

		//https://github.com/haifengl/smile/wiki/Tutorial:-A-Gentle-Introduction-to-Smile
		AttributeDataset dataset = extractDataset(this.mode, false);

		switch (this.mode) {
			case "train":
				return train(dataset);
			case "test":
				return test(dataset);
			default:
				throw new IllegalStateException(
						"Oops. Unknown mode : " + this.mode + " . Choose one of the following modes : " +
								MLMode.values());
		}
	}

	protected MutableTriple<Schema, Iterable<Row>, String> test(AttributeDataset dataset) {
		Predictor<Double> model = deserializeModel();
		double[][] x = dataset.toArray(new double[dataset.size()][]);
		double[] y = dataset.toArray(new double[dataset.size()]);

		int count = x.length;
		double[] predicted = new double[dataset.size()];

		for (int i = 0; i < x.length; i++) {
			predicted[i] = model.predict(x[i]).doubleValue();
		}
		RegressionMeasure accuracy = new MSE();
		double meanSquareError = accuracy.measure(y, predicted);

		Schema schema = Schema.builder()
				.addNullableField("count", Schema.FieldType.DOUBLE)
				.addNullableField("meanSquareError", Schema.FieldType.DOUBLE)
				.build();

		List<Row> a = Arrays.asList(Row.withSchema(schema)
				.addValue(count)
				.addValue(meanSquareError).build());

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
		double[] y = dataset.toArray(new double[dataset.size()]);

		FeatureTransform tf = getTransformTf();

		Attribute[] attributes = dataset.attributes();
		double[][] xt = transform(x, tf, attributes);

		MutableQuadruple<Schema, Iterable<Row>, String, Regression<double[]>>
				tuple = trainTemplate(attributes, xt, y);

		serialize(tuple.fourth, tf);
		return MutableTriple.of(tuple.first, tuple.second, tuple.third);
	}


	/**
	 * Train and serialize model
	 */
	protected abstract MutableQuadruple<Schema, Iterable<Row>, String, Regression<double[]>> trainTemplate(Attribute[] attributes,
																										   double[][] x,
																										   double[] y);


	protected void serialize(Regression<double[]> model, FeatureTransform standardizer) {
		SerializationUtils.serialize(RegressionWrapper.of(model, standardizer), getSerializedModelPath(), true);
	}


}

