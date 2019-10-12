package org.ananas.runner.core.pipeline;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.ananas.runner.core.extension.ExtensionManager;
import org.ananas.runner.core.model.Engine;
import org.apache.beam.runners.dataflow.DataflowRunner;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.runners.flink.FlinkPipelineOptions;
import org.apache.beam.runners.flink.FlinkRunner;
import org.apache.beam.runners.spark.SparkPipelineOptions;
import org.apache.beam.runners.spark.SparkRunner;
import org.apache.beam.sdk.options.PipelineOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PipelineOptionsFactory {

  private static final Logger LOG = LoggerFactory.getLogger(PipelineOptionsFactory.class);

  public static final String FILES_TO_STAGE = "filesToStage";

  public static PipelineOptions create(
      boolean isTest, Engine engine, Set<String> metadataIds, ExtensionManager extensionManager) {
    if (isTest) {
      return createFlinkOptions(null, metadataIds, extensionManager);
    }

    if (engine == null) {
      return createFlinkOptions(null, metadataIds, extensionManager);
    }

    switch (engine.type.toLowerCase()) {
      case "flink":
        return createFlinkOptions(engine, metadataIds, extensionManager);
      case "spark":
        return createSparkOptions(engine, metadataIds, extensionManager);
      case "dataflow":
        return createDataflowOptions(engine, metadataIds, extensionManager);
      default:
        return createFlinkOptions(null, metadataIds, extensionManager);
    }
  }

  public static PipelineOptions createFlinkOptions(
      Engine engine, Set<String> metadataIds, ExtensionManager extensionManager) {
    FlinkPipelineOptions options =
        org.apache.beam.sdk.options.PipelineOptionsFactory.create().as(FlinkPipelineOptions.class);

    // defaut embedded flink engine
    if (engine == null) {
      options.setParallelism(10);
      options.setMaxBundleSize(1000 * 1000L);
      options.setObjectReuse(true);
    } else {
      options.setFlinkMaster(engine.getProperty("flinkMaster", "[auto]"));
      options.setTempLocation(engine.getProperty("tempLocation", "/tmp/"));
      options.setParallelism(engine.getProperty("parallelism", Integer.valueOf(10)));
      options.setMaxBundleSize(engine.getProperty("maxBundleSize", Long.valueOf(1000 * 1000L)));
      options.setObjectReuse(engine.getProperty("objectReuse", Boolean.TRUE));
    }

    options.setAppName(engine == null ? "ananas" : engine.getProperty(Engine.APP_NAME, "ananas"));
    options.setFilesToStage(getFilesToStage(engine, metadataIds, extensionManager));
    options.setRunner(FlinkRunner.class);
    return options;
  }

  public static PipelineOptions createSparkOptions(
      Engine engine, Set<String> metadataIds, ExtensionManager extensionManager) {
    SparkPipelineOptions options =
        org.apache.beam.sdk.options.PipelineOptionsFactory.create().as(SparkPipelineOptions.class);
    options.setFilesToStage(getFilesToStage(engine, metadataIds, extensionManager));

    options.setSparkMaster(engine.getProperty("sparkMaster", "spark://localhost:7077"));
    options.setTempLocation(engine.getProperty("tempLocation", "/tmp/"));
    options.setStreaming(engine.getProperty("streaming", Boolean.FALSE));
    options.setEnableSparkMetricSinks(engine.getProperty("enableMetricSinks", Boolean.TRUE));

    options.setAppName(engine.getProperty(Engine.APP_NAME, "ananas"));
    options.setRunner(SparkRunner.class);

    return options;
  }

  public static PipelineOptions createDataflowOptions(
      Engine engine, Set<String> metadataIds, ExtensionManager extensionManager) {
    DataflowPipelineOptions options =
        org.apache.beam.sdk.options.PipelineOptionsFactory.create()
            .as(DataflowPipelineOptions.class);

    options.setAppName(engine.getProperty(Engine.APP_NAME, "ananas"));
    options.setProject(engine.getProperty("projectId", ""));
    options.setFilesToStage(getFilesToStage(engine, metadataIds, extensionManager));
    options.setTempLocation(engine.getProperty("tempLocation", "gs://cookiesync-gdpr-dev/tmp"));
    options.setRunner(DataflowRunner.class);
    return options;
  }

  private static List<String> getFilesToStage(
      Engine engine, Set<String> metadataIds, ExtensionManager extensionManager) {
    List<String> filesToStaging = getBaseFilesToStage();
    // get files to stage specified for the engine
    if (engine != null) {
      String fileList = engine.getProperty(FILES_TO_STAGE, "");
      LOG.info("engine filesToStage: {}", fileList);
      if (!fileList.equals("")) {
        String[] files = fileList.split(";");
        filesToStaging.addAll(Arrays.asList(files));
      }
    }

    // get step related jars
    metadataIds.forEach(
        id -> {
          if (extensionManager.hasStepMetadata(id)) {
            List<String> classpath =
                extensionManager.getStepMetadata(id).classpath.stream()
                    .map(
                        v -> {
                          try {
                            return new File(v.toURI()).getAbsolutePath();
                          } catch (URISyntaxException e) {
                            e.printStackTrace();
                            return null;
                          }
                        })
                    .filter(v -> v != null)
                    .collect(Collectors.toList());
            LOG.info("step filesToStage: {}", classpath);
            filesToStaging.addAll(classpath);
          }
        });

    return filesToStaging;
  }

  private static List<String> getBaseFilesToStage() {
    List<String> filesToStaging = new ArrayList<>();
    try {
      String jar =
          new File(
                  PipelineOptionsFactory.class
                      .getProtectionDomain()
                      .getCodeSource()
                      .getLocation()
                      .toURI())
              .getPath();
      LOG.info("Detect jar file to staging {}", jar);
      if (jar.endsWith("jar")) {
        LOG.info("Add jar {} to filesToStage list", jar);
        filesToStaging.add(jar);
      }
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }

    // get system defined files to stage settings
    String systemDefinedFiles = System.getProperty(FILES_TO_STAGE);
    LOG.info("System defined filesToStage list: {}", systemDefinedFiles);
    if (systemDefinedFiles != null) {
      String[] files = systemDefinedFiles.split(";");
      filesToStaging.addAll(Arrays.asList(files));
    }

    LOG.info("system filesToStage: {}", String.join(";", filesToStaging));
    return filesToStaging;
  }
}
