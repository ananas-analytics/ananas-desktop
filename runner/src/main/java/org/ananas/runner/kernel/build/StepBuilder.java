package org.ananas.runner.kernel.build;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import org.ananas.runner.kernel.StepRunner;
import org.ananas.runner.kernel.model.Engine;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.kernel.model.StepType;
import org.ananas.runner.kernel.pipeline.PipelineContext;
import org.ananas.runner.kernel.pipeline.PipelineOptionsFactory;
import org.ananas.runner.model.core.StepConfig;
import org.ananas.runner.model.steps.api.APIConnector;
import org.ananas.runner.model.steps.api.APIStepConfig;
import org.ananas.runner.model.steps.concat.ConcatConnector;
import org.ananas.runner.model.steps.dataview.DataViewLoader;
import org.ananas.runner.model.steps.db.*;
import org.ananas.runner.model.steps.db.jdbc.JDBCDriver;
import org.ananas.runner.model.steps.files.*;
import org.ananas.runner.model.steps.files.csv.CSVConnector;
import org.ananas.runner.model.steps.files.csv.CSVStepConfig;
import org.ananas.runner.model.steps.join.JoinConnector;
import org.ananas.runner.model.steps.messaging.kafka.KafkaConnector;
import org.ananas.runner.model.steps.ml.IsStepTrainingMode;
import org.ananas.runner.model.steps.ml.MLModelPredictor;
import org.ananas.runner.model.steps.ml.MLModelTrainer;
import org.ananas.runner.model.steps.scripting.JavascriptRowTransformer;
import org.ananas.runner.model.steps.sql.SQLTransformer;
import org.ananas.runner.model.steps.stat.HistogramTransformer;
import org.apache.beam.sdk.coders.CannotProvideCoderException;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.NullableCoder;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.commons.lang3.tuple.MutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Beam Pipeline builder. For testing purpose, the pipelines has step that fetches data in memory
 * and autodetectBson input and output schemas.
 */
public class StepBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(StepBuilder.class);

  public static org.apache.beam.sdk.Pipeline createPipelineRunner(boolean isTest, Engine engine) {
    PipelineOptions options = PipelineOptionsFactory.create(isTest, engine);
    org.apache.beam.sdk.Pipeline p = org.apache.beam.sdk.Pipeline.create(options);
    Class[] classes =
        new Class[] {
          BigDecimal.class,
          Integer.class,
          String.class,
          Boolean.class,
          Long.class,
          Float.class,
          Double.class,
          Timestamp.class,
          Date.class,
          Byte.class
        };
    for (Class clazz : classes) {
      Coder coder;
      try {
        coder = p.getCoderRegistry().getCoder(clazz);
      } catch (CannotProvideCoderException e) {
        LOG.debug(e.getMessage());
        continue;
      }
      p.getCoderRegistry().registerCoderForClass(clazz, NullableCoder.of(coder));
    }
    return p;
  }

  public static StepRunner join(Step step, StepRunner one, StepRunner other) {
    String leftStepId = (String) step.config.get(StepConfig.JOIN_LEFT_STEPID);
    StepRunner leftStep, rightStep;
    if (one.getStepId().equals(leftStepId)) {
      leftStep = one;
      rightStep = other;
    } else {
      leftStep = other;
      rightStep = one;
    }
    // Map<String, String> columnsMap = new HashMap<>();
    // columnsMap.put("inventoryId", "inventoryId");
    Map<String, String> columnsMap = (Map) step.config.get(StepConfig.JOIN_MAP);
    // List<String> leftColumns = Arrays.asList("inventoryId");
    String joinType =
        (String)
            step.config.getOrDefault(StepConfig.JOIN_TYPE, JoinConnector.JoinType.LEFT_JOIN.name);
    List<String> leftColumns = (List) step.config.get(StepConfig.JOIN_LEFT_COLUMNS);
    List<String> rightColumns = (List) step.config.get(StepConfig.JOIN_RIGHT_COLUMNS);
    // List<String> rightColumns = Arrays.asList("inventoryId");
    return new JoinConnector(
        step.id, joinType, leftStep, rightStep, leftColumns, rightColumns, columnsMap);
  }

  public static StepRunner concat(Step step, StepRunner one, StepRunner other) {
    String leftStepId = (String) step.config.get(StepConfig.JOIN_LEFT_STEPID);
    StepRunner leftStep, rightStep;
    if (one.getStepId().equals(leftStepId)) {
      leftStep = one;
      rightStep = other;
    } else {
      leftStep = other;
      rightStep = one;
    }
    return new ConcatConnector(step.id, leftStep, rightStep);
  }

  public static StepRunner connector(
      Step step, PipelineContext context, boolean doSampling, boolean isTest) {
    switch ((String) step.config.get(StepConfig.SUBTYPE)) {
      case "mongo":
        MongoStepConfig c = new MongoStepConfig(step.config);
        return new MongoDBConnector(context.getPipeline(), step.id, c, doSampling, isTest);
      case "api":
        APIStepConfig apiStepConfig = new APIStepConfig(step.config);
        return new APIConnector(step.id, context.getPipeline(), apiStepConfig);
      case "jdbc":
        JdbcStepConfig ci = new JdbcStepConfig(step.config);
        return new JdbcConnector(context.getPipeline(), step.id, ci, doSampling, isTest);
      case "file":
        switch ((String) step.config.get(StepConfig.FORMAT)) {
          case "csv":
            CSVStepConfig csvConfig = new CSVStepConfig(StepType.Connector, step.config);
            return new CSVConnector(step.id, csvConfig, context.getPipeline(), doSampling, isTest);
          case "text":
            return new TextConnector(
                step.id,
                (String) step.config.get(StepConfig.PATH),
                context.getPipeline(),
                doSampling,
                isTest);
          case "json":
            return new JsonConnector(
                context.getPipeline(),
                step.id,
                (String) step.config.get(StepConfig.PATH),
                doSampling,
                isTest);
          case "excel":
            ExcelStepConfig excelConfig = new ExcelStepConfig(step.config);
            return new ExcelConnector(
                step.id, context.getPipeline(), excelConfig, doSampling, isTest);
          case "any":
            return new TikaConnector(
                step.id,
                (String) step.config.get(StepConfig.PATH),
                context.getPipeline(),
                doSampling,
                isTest);
          default:
            throw new IllegalStateException(
                "Unsupported files format '" + step.config.get(StepConfig.FORMAT) + "'");
        }
      case "kafka":
        switch ((String) step.config.get(StepConfig.FORMAT)) {
          case "json":
            return new KafkaConnector(
                context.getPipeline(),
                step.id,
                "kafka01.dev.uswest2.dmxleo.internal:9092,kafka02.dev.uswest2.dmxleo.internal:9092,kafka03.dev.uswest2.dmxleo.internal:9092",
                Arrays.asList("uswest2.reporting.auctiondata"),
                "test");
          default:
            throw new IllegalStateException(
                "Unsupported Kafka message format '" + step.config.get(StepConfig.FORMAT) + "'");
        }
        /* testing events
        "uswest2.reporting.auctiondata"
                     "vast.AdClick", "vast.AdClose", "vast.AdCollapse", "vast.AdComplete", "vast.AdError", "vast.AdExpand", "vast.AdFirstQuartile",
        "vast.AdImpression", "vast.AdMidpoint", "vast.AdMinimize", "vast.AdProgress", "vast.AdStart", "vast.AdThirdQuartile",
        "vast.AdView30Seconds", "vast.AdOverlayViewDuration", "vast.AdAcceptInvitation", "vast.AdCreativeView", "vast.AdTimeSpentViewing",
        "vast.PlayerMute", "vast.PlayerPause", "vast.PlayerResume", "vast.PlayerRewind", "vast.PlayerSkip", "vast.PlayerUnmute"
                     */

      default:
        throw new IllegalStateException("Unsupported connector type '" + step.type + "'");
    }
  }

  public static StepRunner append(Step step, StepRunner previous, boolean isTest) {
    switch (step.type) {
      case StepConfig.TYPE_TRANSFORMER:
        return transformer(step, previous, isTest);
      case StepConfig.TYPE_LOADER:
        return loader(step, previous, isTest);
      case StepConfig.TYPE_DATAVIEWER:
        return dataViewer(step, previous, isTest);
      default:
        throw new IllegalStateException("Unsupported transformer/loader type '" + step.type + "'");
    }
  }

  public static StepRunner transformer(Step step, StepRunner previous, boolean isTest) {
    switch ((String) step.config.get(StepConfig.SUBTYPE)) {
      case "sql":
        return new SQLTransformer(step.id, (String) step.config.get(StepConfig.SQL), previous);
      case "js":
        return new JavascriptRowTransformer(
            step.id,
            previous,
            "transform",
            (String) step.config.getOrDefault(StepConfig.JAVASCRIPT_SAMPLE, "[]"),
            Arrays.asList((String) step.config.get(StepConfig.JAVASCRIPT_SCRIPT)));
      case "histogram":
        return new HistogramTransformer(step, previous);
      default:
        throw new IllegalStateException(
            "Unsupported step type '" + step.config.get(StepConfig.SUBTYPE) + "'");
    }
  }

  public static StepRunner dataViewer(Step step, StepRunner previous, boolean isTest) {
    if (isTest) {
      StepRunner sqlQuery =
          new SQLTransformer(step.id, (String) step.config.get(StepConfig.DATAVIEW_SQL), previous);

      return sqlQuery;
    }
    return new DataViewLoader(step.id, previous, false);
  }

  public static StepRunner loader(Step step, StepRunner input, boolean isTest) {
    switch ((String) step.config.get(StepConfig.SUBTYPE)) {
      case "file":
        switch ((String) step.config.get(StepConfig.FORMAT)) {
          case "json":
            return new FileLoader(
                input,
                step.id,
                FileLoader.SupportedFormat.JSON,
                (String) step.config.get(StepConfig.PATH),
                (String) step.config.get(StepConfig.SHARD),
                (String) step.config.get(StepConfig.PREFIX),
                false,
                isTest);
          case "csv":
            return new FileLoader(
                input,
                step.id,
                FileLoader.SupportedFormat.CSV,
                (String) step.config.get(StepConfig.PATH),
                (String) step.config.get(StepConfig.SHARD),
                (String) step.config.get(StepConfig.PREFIX),
                (Boolean) step.config.getOrDefault(StepConfig.IS_HEADER, false),
                isTest);
          default:
            throw new IllegalStateException(
                "Unsupported step config subtype '" + step.config.get(StepConfig.FORMAT) + "'");
        }
      case "jdbc":
        JDBCDriver jdbcDriver =
            JDBCDriver.NONE.getDriverByName((String) step.config.get(StepConfig.JDBC_TYPE));
        boolean overwrite = (boolean) step.config.getOrDefault(StepConfig.JDBC_OVERWRITE, false);

        return new JdbcLoader(
            step.id,
            overwrite,
            (String) step.config.get(StepConfig.JDBC_TABLENAME),
            jdbcDriver,
            "jdbc:" + (String) step.config.get(StepConfig.JDBC_URL),
            (String) step.config.get(StepConfig.JDBC_USER),
            (String) step.config.get(StepConfig.JDBC_PASSWORD),
            input,
            isTest);
      default:
        throw new IllegalStateException(
            "Unsupported subtype'" + step.config.get(StepConfig.SUBTYPE) + "'");
    }
  }

  public static StepRunner mlTransformer(
      Step step,
      PipelineContext ctxt,
      StepRunner previous,
      boolean isTest,
      Set<MutablePair<Step, Schema>> mlSources) {
    if (IsStepTrainingMode.of().filter(step)) {
      return new MLModelTrainer(ctxt, step, isTest, mlSources);
    } else {
      return new MLModelPredictor(
          step, previous); // Note here the context is the previous step context
    }
  }
}
