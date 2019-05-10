package org.ananas.runner.model.steps.commons.execution;


import org.apache.beam.runners.flink.FlinkPipelineOptions;
import org.apache.beam.runners.flink.FlinkRunner;
import org.apache.beam.runners.spark.SparkPipelineOptions;
import org.apache.beam.runners.spark.SparkRunner;
import org.apache.beam.sdk.options.PipelineOptions;

public class PipelineOptionsFactory {

	public static PipelineOptions create(boolean isTest) {
		return isTest ? createFlinkOptions() : createSparkOptions();
	}


	public static PipelineOptions createFlinkOptions() {
		FlinkPipelineOptions options =
				org.apache.beam.sdk.options.PipelineOptionsFactory.create().as(FlinkPipelineOptions.class);
		options.setParallelism(10);
		options.setMaxBundleSize(1000 * 1000L);
		options.setObjectReuse(true);


		options.setRunner(FlinkRunner.class);
		return options;
	}



   public static PipelineOptions createSparkOptions() {
	   SparkPipelineOptions options = org.apache.beam.sdk.options.PipelineOptionsFactory.create().as(SparkPipelineOptions.class);
	   options.setSparkMaster("spark://grego-Latitude-7480:7077");
 	   //spark/sbin/start-master.sh
	   //spark/sbin/start-slave.sh spark://grego-Latitude-7480:7077
	   options.setTempLocation("/tmp/");
	   options.setAppName("ananas");
	   options.setStreaming(false);
	   options.setEnableSparkMetricSinks(true);

	   options.setRunner(SparkRunner.class);

	   return options;
    }



}
