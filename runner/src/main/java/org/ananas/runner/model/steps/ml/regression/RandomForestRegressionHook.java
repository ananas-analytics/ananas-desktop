package org.ananas.runner.model.steps.ml.regression;

import com.google.common.base.Preconditions;
import org.ananas.runner.model.core.Step;
import org.ananas.runner.model.core.StepConfig;
import org.ananas.runner.model.steps.ml.MLModelTrainer;
import org.ananas.runner.model.steps.ml.regression.common.RegressionLinearHook;
import org.ananas.runner.misc.MutableQuadruple;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;
import smile.data.Attribute;
import smile.regression.OLS;
import smile.regression.RandomForest;
import smile.regression.Regression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * OLS
 */
public class RandomForestRegressionHook extends RegressionLinearHook<OLS> {


	public RandomForestRegressionHook(String mode,
									  Pipeline pipeline,
									  Map<String, Schema> schemas,
									  Map<String, Step> steps,
									  Map<String, String> modesteps,
									  Step mlStep,
									  MLModelTrainer blackBoxTransformer) {
		super(mode, pipeline, schemas, steps, modesteps, mlStep, blackBoxTransformer);
	}


	@Override
	protected MutableQuadruple<Schema, Iterable<Row>, String, Regression<double[]>> trainTemplate(Attribute[] attributes,
																								  double[][] x,
																								  double[] y) {
		Integer ntrees = (Integer) this.mlStep.config.get(StepConfig.ML_TREES);
		Preconditions.checkNotNull(ntrees, "Please choose the number of trees / " + StepConfig.ML_TREES);

		RandomForest model = new RandomForest(x, y, ntrees.intValue());

		Schema schema = Schema.builder()
				.addNullableField("Std Error", Schema.FieldType.DOUBLE)
				.build();

		List<Row> a = new ArrayList<>();

		a.add(Row.withSchema(schema)
				.addValue(model.error())
				.build()
		);
		return MutableQuadruple.of(schema, a, "Training completed; Results : " + model.toString(), model);
	}


}
