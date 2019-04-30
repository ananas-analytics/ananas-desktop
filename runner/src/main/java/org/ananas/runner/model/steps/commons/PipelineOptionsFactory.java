package org.ananas.runner.model.steps.commons;

import org.apache.beam.runners.flink.FlinkPipelineOptions;
import org.apache.beam.runners.flink.FlinkRunner;
import org.apache.beam.sdk.options.PipelineOptions;

public class PipelineOptionsFactory {

	public static PipelineOptions create(boolean isTest) {
		FlinkPipelineOptions options =
				org.apache.beam.sdk.options.PipelineOptionsFactory.create().as(FlinkPipelineOptions.class);
		options.setParallelism(10);
		options.setMaxBundleSize(1000 * 1000L);
		options.setObjectReuse(true);


		options.setRunner(FlinkRunner.class);
		return options;
	}

    /*private static PipelineOptions getSparkPipelineOptions(String[] args) {
			SparkContextOptions ret1 = org.apache.beam.sdk.options.PipelineOptionsFactory.fromArgs(args).as(SparkContextOptions.class);
            SparkConf conf1 = getConf(false);
            JavaSparkContext context1 = new JavaSparkContext(conf1);
            ret1.setProvidedSparkContext(context1);
            ret1.setRunner(SparkRunner.class);
            return ret1;

    }

    private static SparkConf getConf(boolean local) {
        SparkConf conf = new SparkConf();
        if (local) {
            conf.setMaster("local[3]");
            conf.setAppName("LocalBeam");
        }
        conf.set("spark.kryo.registrationRequired", "true");
        conf.set("spark.serializer", KryoSerializer.class.getName());
        conf.set("spark.kryo.registrator", BeamSparkRunnerRegistrator.class.getName());
        conf.registerKryoClasses(new Class[]{
                scala.collection.mutable.WrappedArray.ofRef.class,
                Object[].class,
                org.apache.beam.runners.spark.util.ByteArray.class});
        return conf;
    }*/


}
