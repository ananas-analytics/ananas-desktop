package org.ananas.runner.core.build;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.ananas.runner.core.ConcatStepRunner;
import org.ananas.runner.core.JoinStepRunner;
import org.ananas.runner.core.StepRunner;
import org.ananas.runner.core.errors.AnanasException;
import org.ananas.runner.core.errors.ExceptionHandler.ErrorCode;
import org.ananas.runner.core.extension.ExtensionManager;
import org.ananas.runner.core.extension.ExtensionRegistry;
import org.ananas.runner.core.model.Engine;
import org.ananas.runner.core.model.Step;
import org.ananas.runner.core.pipeline.PipelineContext;
import org.ananas.runner.core.pipeline.PipelineOptionsFactory;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.CannotProvideCoderException;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.NullableCoder;
import org.apache.beam.sdk.options.PipelineOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StepBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(StepBuilder.class);

  public static final String TYPE_CONNECTOR = "connector";
  public static final String TYPE_TRANSFORMER = "transformer";
  public static final String TYPE_LOADER = "loader";
  public static final String TYPE_DATAVIEWER = "viewer";

  public static org.apache.beam.sdk.Pipeline createPipelineRunner(
      boolean isTest, Engine engine, ExtensionManager extensionManager) {
    return createPipelineRunner(isTest, engine, null, extensionManager);
  }

  public static org.apache.beam.sdk.Pipeline createPipelineRunner(
      boolean isTest, Engine engine, Set<Step> steps, ExtensionManager extensionManager) {

    Set<String> metadataIds =
        steps == null
            ? new HashSet<>()
            : steps.stream().map(step -> step.metadataId).collect(Collectors.toSet());

    PipelineOptions options =
        PipelineOptionsFactory.create(isTest, engine, metadataIds, extensionManager);
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

  public static StepRunner connector(
      ExtensionManager extensionManager,
      Engine engine,
      Step step,
      PipelineContext context,
      boolean doSampling,
      boolean isTest) {
    if (!ExtensionRegistry.hasStep(step.metadataId, extensionManager)) {
      throw new AnanasException(
          ErrorCode.DAG, "No StepRunner is registered for meta id: " + step.metadataId);
    }

    Class<? extends StepRunner> clazz =
        ExtensionRegistry.getStep(step.metadataId, isLocalEngine(engine, isTest), extensionManager);

    try {
      Constructor<? extends StepRunner> ctor =
          clazz.getDeclaredConstructor(Pipeline.class, Step.class, Boolean.TYPE, Boolean.TYPE);
      ctor.setAccessible(true);
      StepRunner connector = ctor.newInstance(context.getPipeline(), step, doSampling, isTest);
      connector.setExtensionManager(extensionManager);
      connector.build();
      return connector;
    } catch (InstantiationException
        | NoSuchMethodException
        | IllegalAccessException
        | InvocationTargetException e) {
      System.out.println(e.getMessage());
      throw new AnanasException(ErrorCode.DAG, e.getLocalizedMessage());
    }
  }

  public static StepRunner transformer(
      ExtensionManager extensionManager,
      Engine engine,
      Step step,
      StepRunner previous,
      boolean isTest) {
    if (!ExtensionRegistry.hasStep(step.metadataId, extensionManager)) {
      throw new AnanasException(
          ErrorCode.DAG, "No StepRunner is registered for meta id: " + step.metadataId);
    }

    Class<? extends StepRunner> clazz =
        ExtensionRegistry.getStep(step.metadataId, isLocalEngine(engine, isTest), extensionManager);

    try {
      Constructor<? extends StepRunner> ctor =
          clazz.getDeclaredConstructor(Step.class, StepRunner.class);
      ctor.setAccessible(true);
      StepRunner transformer = ctor.newInstance(step, previous);
      transformer.setExtensionManager(extensionManager);
      transformer.build();
      return transformer;
    } catch (InstantiationException
        | NoSuchMethodException
        | IllegalAccessException
        | InvocationTargetException e) {
      System.out.println(e.getMessage());
      throw new AnanasException(ErrorCode.DAG, e.getLocalizedMessage());
    }
  }

  public static StepRunner loader(
      ExtensionManager extensionManager,
      Engine engine,
      Step step,
      StepRunner previous,
      boolean isTest) {
    if (!ExtensionRegistry.hasStep(step.metadataId, extensionManager)) {
      throw new AnanasException(
          ErrorCode.DAG, "No StepRunner is registered for meta id: " + step.metadataId);
    }

    Class<? extends StepRunner> clazz =
        ExtensionRegistry.getStep(step.metadataId, isLocalEngine(engine, isTest), extensionManager);

    try {
      Constructor<? extends StepRunner> ctor =
          clazz.getDeclaredConstructor(Step.class, StepRunner.class, Boolean.TYPE);
      ctor.setAccessible(true);
      StepRunner loader = ctor.newInstance(step, previous, isTest);
      loader.setExtensionManager(extensionManager);
      loader.build();
      return loader;
    } catch (InstantiationException
        | NoSuchMethodException
        | IllegalAccessException
        | InvocationTargetException e) {
      System.out.println(e.getMessage());
      throw new AnanasException(ErrorCode.DAG, e.getLocalizedMessage());
    }
  }

  public static StepRunner dataViewer(
      String jobId,
      ExtensionManager extensionManager,
      Engine engine,
      Step step,
      StepRunner previous,
      boolean isTest) {
    if (!ExtensionRegistry.hasStep(step.metadataId, extensionManager)) {
      throw new AnanasException(
          ErrorCode.DAG, "No StepRunner is registered for meta id: " + step.metadataId);
    }

    Class<? extends StepRunner> clazz =
        ExtensionRegistry.getStep(step.metadataId, isLocalEngine(engine, isTest), extensionManager);

    try {
      Constructor<? extends StepRunner> ctor =
          clazz.getDeclaredConstructor(
              Step.class, StepRunner.class, Engine.class, String.class, Boolean.TYPE);
      ctor.setAccessible(true);
      StepRunner viewer = ctor.newInstance(step, previous, engine, jobId, isTest);
      viewer.setExtensionManager(extensionManager);
      viewer.build();
      return viewer;
    } catch (InstantiationException
        | NoSuchMethodException
        | IllegalAccessException
        | InvocationTargetException e) {
      System.out.println(e.getMessage());
      throw new AnanasException(ErrorCode.DAG, e.getLocalizedMessage());
    }
  }

  public static StepRunner append(
      String jobId,
      ExtensionManager extensionManager,
      Engine engine,
      Step step,
      StepRunner previous,
      boolean isTest) {
    // TODO: maybe pass engine to all steps? Engine settings might be useful for all step types
    switch (step.type) {
      case TYPE_TRANSFORMER:
        return transformer(extensionManager, engine, step, previous, isTest);
      case TYPE_LOADER:
        return loader(extensionManager, engine, step, previous, isTest);
      case TYPE_DATAVIEWER:
        return dataViewer(jobId, extensionManager, engine, step, previous, isTest);
      default:
        throw new IllegalStateException("Unsupported transformer/loader type '" + step.type + "'");
    }
  }

  public static StepRunner join(Engine engine, Step step, StepRunner one, StepRunner another) {
    StepRunner joinStepRunner = new JoinStepRunner(step, one, another);
    joinStepRunner.build();
    return joinStepRunner;
  }

  public static StepRunner concat(Engine engine, Step step, List<StepRunner> upstreams) {
    StepRunner concatStepRunner = new ConcatStepRunner(step, upstreams);
    concatStepRunner.build();
    return concatStepRunner;
  }

  public static boolean isLocalEngine(Engine engine, boolean isTest) {
    if (isTest) {
      return true;
    }

    if (engine == null) {
      return true;
    }

    if (engine.type.toLowerCase().equals("flink")) {
      String flinkMaster = engine.getProperty("flinkMaster", "[auto]");
      if (flinkMaster.equals("[auto]") || flinkMaster.equals("[local")) {
        return true;
      }
    }

    return false;
  }
}
