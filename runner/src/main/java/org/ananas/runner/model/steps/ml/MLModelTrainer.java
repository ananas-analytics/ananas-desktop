package org.ananas.runner.model.steps.ml;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import org.ananas.runner.model.core.PipelineContext;
import org.ananas.runner.model.core.PipelineHook;
import org.ananas.runner.model.core.Step;
import org.ananas.runner.model.core.StepConfig;
import org.ananas.runner.model.steps.commons.AbstractStepRunner;
import org.ananas.runner.model.steps.commons.StepRunner;
import org.ananas.runner.model.steps.commons.StepType;
import org.ananas.runner.model.steps.ml.classifier.*;
import org.ananas.runner.model.steps.ml.cluster.*;
import org.ananas.runner.model.steps.ml.featureselection.GAFeatureSelectionHook;
import org.ananas.runner.model.steps.ml.regression.*;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.commons.lang3.tuple.MutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smile.classification.*;
import smile.clustering.*;
import smile.regression.LASSO;
import smile.regression.OLS;
import smile.regression.RidgeRegression;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MLModelTrainer extends AbstractStepRunner implements StepRunner, Serializable {
	private static final Logger LOG = LoggerFactory.getLogger(MLModelTrainer.class);
	private static final long serialVersionUID = -2846511153859594113L;

	public static final Map<String, Class> ALGOS_CLUSTERING = ImmutableMap.<String, Class>builder()
			// clustering
			.put("kmeans", KMeans.class)
			.put("xmeans", XMeans.class)
			.put("gmeans", GMeans.class)
			.put("deterministicannealing", DeterministicAnnealing.class)
			.put("clarans", CLARANS.class).build();

	//feature selection
	public static final Map<String, Class> ALGOS_FT = ImmutableMap.<String, Class>builder()
			.put("gafeatureselection", GAFeatureSelectionHook.class).build();


	//classifier
	public static final Map<String, Class> ALGOS_CLASSIFIER = ImmutableMap.<String, Class>builder()
			.put("adaboost", AdaBoost.Trainer.class)
			.put("gradienttreeboost", GradientTreeBoost.Trainer.class)
			.put("decisiontree", DecisionTree.Trainer.class)
			.put("knn", KNN.Trainer.class)
			.put("logisticregression", LogisticRegression.Trainer.class)
			.put("neuralnetwork", NeuralNetwork.Trainer.class)
			.put("randomforest", RandomForest.Trainer.class)
			.put("rda", RDA.Trainer.class)
			.build();

	public static final Map<String, Class> ALGOS_REGRESSION = ImmutableMap.<String, Class>builder()
			//regression
			.put("gbregression", smile.regression.GradientTreeBoost.Trainer.class)
			.put("lasso", LASSO.Trainer.class)
			.put("ols", OLS.Trainer.class)
			.put("randomeforestregression", smile.regression.RandomForest.Trainer.class)
			.put("ridge", RidgeRegression.Trainer.class)
			.build();

	public static Map<String, Class> ALGOS = ImmutableMap.<String, Class>builder()
			.putAll(ALGOS_CLASSIFIER)
			.putAll(ALGOS_CLUSTERING)
			.putAll(ALGOS_FT)
			.putAll(ALGOS_REGRESSION).build();


	protected boolean isTest;

	Map<String, Step> steps;
	Map<String, Schema> schemas;
	String mode;
	Map<String, String> modeStep;


	public MLModelTrainer(PipelineContext ctxt,
						  Step MLStep,
						  boolean isTest,
						  Set<MutablePair<Step, Schema>> mlSources) {
		super(StepType.Transformer);
		this.stepId = MLStep.id;
		this.isTest = isTest;
		this.steps = new HashMap<>();
		this.schemas = new HashMap<>();

		this.mode = (String) MLStep.config.get("mode");
		this.modeStep = new HashMap<>();
		this.modeStep.put(MLHookTemplate.MLMode.TRAIN.name().toLowerCase(),
				(String) MLStep.config.get(StepConfig.TRAIN_ID));
		this.modeStep.put(MLHookTemplate.MLMode.PREDICT.name().toLowerCase(),
				(String) MLStep.config.get(StepConfig.PREDICT_ID));
		this.modeStep.put(MLHookTemplate.MLMode.TEST.name().toLowerCase(),
				(String) MLStep.config.get(StepConfig.TEST_ID));

		for (MutablePair<Step, Schema> s : mlSources) {
			this.steps.put(s.getLeft().id, s.getLeft());
			this.schemas.put(s.getLeft().id, s.getRight());
		}

		if (!isTest && this.mode.equalsIgnoreCase("predict")) {
			throw new IllegalArgumentException("Runnning a train step is not supported");
		}

		ctxt.setHook(create(ctxt.getPipeline(), MLStep, this));
	}

	protected PipelineHook create(
			Pipeline pipeline,
			Step mlStep,
			MLModelTrainer blackBoxTransformer) {
		String algo = (String) mlStep.config.get(StepConfig.ML_ALGO);
		switch (algo) {
			case "kmeans":
				return new KmeansHook(this.mode,
						pipeline,
						this.schemas,
						this.steps,
						this.modeStep,
						mlStep,
						blackBoxTransformer);
			case "adaboost":
				return new AdaBoostHook(this.mode,
						pipeline,
						this.schemas,
						this.steps,
						this.modeStep,
						mlStep,
						blackBoxTransformer);
			case "gafeatureselection":
				return new GAFeatureSelectionHook(this.mode,
						pipeline,
						this.schemas,
						this.steps,
						this.modeStep,
						mlStep,
						blackBoxTransformer);
			case "gradienttreeboost":
				return new GradientTreeBoostHook(this.mode,
						pipeline,
						this.schemas,
						this.steps,
						this.modeStep,
						mlStep,
						blackBoxTransformer);
			case "decisiontree":
				return new DecisionTreeBoostHook(this.mode,
						pipeline,
						this.schemas,
						this.steps,
						this.modeStep,
						mlStep,
						blackBoxTransformer);
			case "knn":
				return new KNNHook(this.mode,
						pipeline,
						this.schemas,
						this.steps,
						this.modeStep,
						mlStep,
						blackBoxTransformer);
			case "logisticregression":
				return new LogisticRegressionHook(this.mode,
						pipeline,
						this.schemas,
						this.steps,
						this.modeStep,
						mlStep,
						blackBoxTransformer);
			case "neuralnetwork":
				return new NeuralNetworkHook(this.mode,
						pipeline,
						this.schemas,
						this.steps,
						this.modeStep,
						mlStep,
						blackBoxTransformer);
			case "randomforest":
				return new RandomForestHook(this.mode,
						pipeline,
						this.schemas,
						this.steps,
						this.modeStep,
						mlStep,
						blackBoxTransformer);
			case "rda":
				return new RDAHook(this.mode,
						pipeline,
						this.schemas,
						this.steps,
						this.modeStep,
						mlStep,
						blackBoxTransformer);
			case "xmeans":
				return new XmeansHook(this.mode,
						pipeline,
						this.schemas,
						this.steps,
						this.modeStep,
						mlStep,
						blackBoxTransformer);
			case "gmeans":
				return new GmeansHook(this.mode,
						pipeline,
						this.schemas,
						this.steps,
						this.modeStep,
						mlStep,
						blackBoxTransformer);
			case "deterministicannealing":
				return new DacHook(this.mode,
						pipeline,
						this.schemas,
						this.steps,
						this.modeStep,
						mlStep,
						blackBoxTransformer);
			case "clarans":
				return new ClaransHook(this.mode,
						pipeline,
						this.schemas,
						this.steps,
						this.modeStep,
						mlStep,
						blackBoxTransformer);
			case "gbregression":
				return new GradientBoostingRegressionHook(this.mode,
						pipeline,
						this.schemas,
						this.steps,
						this.modeStep,
						mlStep,
						blackBoxTransformer);
			case "lasso":
				return new LassoRegressionHook(this.mode,
						pipeline,
						this.schemas,
						this.steps,
						this.modeStep,
						mlStep,
						blackBoxTransformer);
			case "ols":
				return new OLSHook(this.mode,
						pipeline,
						this.schemas,
						this.steps,
						this.modeStep,
						mlStep,
						blackBoxTransformer);
			case "randomeforestregression":
				return new RandomForestRegressionHook(this.mode,
						pipeline,
						this.schemas,
						this.steps,
						this.modeStep,
						mlStep,
						blackBoxTransformer);
			case "ridge":
				return new RidgeRegressionHook(this.mode,
						pipeline,
						this.schemas,
						this.steps,
						this.modeStep,
						mlStep,
						blackBoxTransformer);
			default:
				throw new RuntimeException(
						"config.algo is empty. Choose one algo :  " + Joiner.on("    ").join(this.ALGOS.keySet()));

		}
	}
}

