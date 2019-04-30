package org.ananas.runner.model.steps.ml.regression;

import com.google.common.base.Joiner;
import org.ananas.runner.model.core.Step;
import org.ananas.runner.model.steps.ml.MLModelTrainer;
import org.ananas.runner.model.steps.ml.regression.common.RegressionLinearHook;
import org.ananas.runner.utils.MutableQuadruple;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;
import smile.data.Attribute;
import smile.math.Math;
import smile.regression.OLS;
import smile.regression.Regression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * OLS
 */
public class OLSHook extends RegressionLinearHook<OLS> {


	public OLSHook(String mode,
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


		OLS model = new OLS(x, y);

		Schema schema = Schema.builder()
				.addNullableField("residual sum of squares", Schema.FieldType.DOUBLE)
				.addNullableField("Ftest", Schema.FieldType.DOUBLE)
				.addNullableField("Rsquared", Schema.FieldType.DOUBLE)
				.addNullableField("adjustedRsquared", Schema.FieldType.DOUBLE)
				.addNullableField("df", Schema.FieldType.INT32)
				.addNullableField("residual SumOfSquares", Schema.FieldType.DOUBLE)
				.addNullableField("residual Std Error", Schema.FieldType.DOUBLE)
				.addNullableField("residuals Min", Schema.FieldType.DOUBLE)
				.addNullableField("residuals 1Q", Schema.FieldType.DOUBLE)
				.addNullableField("residuals Median", Schema.FieldType.DOUBLE)
				.addNullableField("residuals 3Q", Schema.FieldType.DOUBLE)
				.addNullableField("residuals Max", Schema.FieldType.DOUBLE)
				.addNullableField("fitted values", Schema.FieldType.STRING)

				.build();

		List<Row> a = new ArrayList<>();
		double[] r = model.residuals().clone();

		a.add(Row.withSchema(schema)
				.addValue(model.RSS())
				.addValue(model.ftest())
				.addValue(model.RSquared())
				.addValue(model.adjustedRSquared())
				.addValue(model.df())
				.addValue(model.error())
				.addValue(Math.min(r))
				.addValue(Math.q1(r))
				.addValue(Math.median(r))
				.addValue(Math.q3(r))
				.addValue(Math.max(r))
				.addValue(Joiner.on(" ").join(Arrays.asList(model.fittedValues())))
				.build()
		);
		return MutableQuadruple.of(schema, a, "Training completed; Results : " + model.toString(), this.mode);
	}


}
