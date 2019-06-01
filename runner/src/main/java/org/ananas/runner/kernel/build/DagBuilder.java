package org.ananas.runner.kernel.build;

import com.google.common.base.Preconditions;
import java.util.*;
import org.ananas.runner.kernel.StepRunner;
import org.ananas.runner.kernel.model.AnanasGraph;
import org.ananas.runner.kernel.model.Dag;
import org.ananas.runner.kernel.model.DagRequest;
import org.ananas.runner.kernel.model.Dataframe;
import org.ananas.runner.kernel.model.Engine;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.kernel.model.Variable;
import org.ananas.runner.kernel.pipeline.NoHook;
import org.ananas.runner.kernel.pipeline.PipelineContext;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.commons.lang3.tuple.MutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DagBuilder implements Builder {

  private static final Logger LOG = LoggerFactory.getLogger(DagBuilder.class);

  private AnanasGraph dag;

  private Set<String> stepIds;

  private Engine engine;

  // private static Cache<String, Iterable<Step>> stepsCache =
  // CacheBuilder.newBuilder().maximumSize(100).expireAfterWrite(10, TimeUnit.MINUTES).build();

  private boolean isTest;
  private Map<String, Variable> variables;

  public DagBuilder(DagRequest dagRequest, boolean isTest) {
    this(dagRequest.dag, dagRequest.goals, dagRequest.params, dagRequest.engine, isTest);
  }

  private DagBuilder(
      Dag d, Set<String> goals, Map<String, Variable> variables, Engine engine, boolean isTest) {
    this.dag = new AnanasGraph(d, goals).reverse().subDag(goals).reverse();
    System.out.println(this.dag);
    this.stepIds = goals;
    this.isTest = isTest;
    Preconditions.checkNotNull(variables);
    this.variables = variables == null ? new HashMap<>() : variables;
    this.engine = engine;
  }

  public Set<String> getGoals() {
    return this.stepIds;
  }

  public Map<String, Dataframe> test() {
    Map<String, Dataframe> p = new HashMap<>();

    // build
    MutablePair<Map<String, StepRunner>, Stack<PipelineContext>> runnableDag = build();
    Map<String, StepRunner> stepRunners = runnableDag.getLeft();
    Stack<PipelineContext> contexts = runnableDag.getRight();

    // run test
    for (PipelineContext context : contexts) {
      context.getHook().run();
      context.getPipeline().run().waitUntilFinish();
    }

    // Fetch results
    for (String stepId : this.stepIds) {
      StepRunner stepRunner = stepRunners.get(stepId);
      if (stepRunner == null) {
        throw new RuntimeException("Ooops . Somthing went wrong with step " + stepId);
      }
      // TODO: fix this, unify the way to get step schema
      List<List<Object>> data =
          this.isTest && stepRunner.getReader() != null
              ? stepRunner.getReader().getData()
              : new LinkedList<>();
      p.put(
          stepId,
          data == null
              ? null
              : Dataframe.Of(stepId, stepRunner.getSchema(), data, stepRunner.getMessage()));
    }
    LOG.info(String.format("DAG Contexts size %d", contexts.size()));
    LOG.info(String.format("DAG Run %s ", this.dag.toString()));
    return p;
  }

  public MutablePair<Map<String, StepRunner>, Stack<PipelineContext>> build() {
    Map<String, StepRunner> stepRunnerMap = new HashMap<>();
    Stack<PipelineContext> contexts = new Stack<>();
    Set<Step> topologicallySortedSteps = this.dag.topologicalSort();
    for (Step step : topologicallySortedSteps) {
      StepRunner stepRunner;
      Set<Step> predecessors = this.dag.predecessors(step);
      switch (predecessors.size()) {
        case 0:
          if (contexts.empty()) {
            PipelineContext ctxt =
                PipelineContext.of(
                    new NoHook(), StepBuilder.createPipelineRunner(this.isTest, this.engine));
            contexts.push(ctxt);
          }
          stepRunner = StepBuilder.connector(step, contexts.peek(), this.isTest, this.isTest);
          stepRunnerMap.put(step.id, stepRunner);
          break;
        case 1:
          // if one predecessor
          stepRunner =
              StepBuilder.append(
                  step, stepRunnerMap.get(predecessors.iterator().next().id), this.isTest);
          break;
        case 2:
          // if two predecessors then join both
          if (step.config.get("subtype").equals("join")) {
            Iterator<Step> it = predecessors.iterator();
            StepRunner one = stepRunnerMap.get(it.next().id);
            StepRunner other = stepRunnerMap.get(it.next().id);
            stepRunner = StepBuilder.join(step, one, other);
          } else if (step.config.get("subtype").equals("concat")) {
            Iterator<Step> it = predecessors.iterator();
            StepRunner one = stepRunnerMap.get(it.next().id);
            StepRunner other = stepRunnerMap.get(it.next().id);
            stepRunner = StepBuilder.concat(step, one, other);
          } else if (step.config.get("subtype").equals("ml")) {
            stepRunner = mlStep(stepRunnerMap, contexts, step, predecessors);
          } else {
            throw new RuntimeException(
                String.format(
                    "Ooops something wrong. A join or ml step is required here because there are two predecessors.",
                    step.id));
          }
          break;
        case 3:
          if (step.config.get("subtype").equals("ml")) {
            stepRunner = mlStep(stepRunnerMap, contexts, step, predecessors);
          } else {
            throw new RuntimeException(
                String.format(
                    "Ooops something wrong. An ML step is required here because there are 3 predecessors.",
                    step.id));
          }
          break;
        default:
          throw new RuntimeException(
              String.format("Step %s has more than 3 predecessors. Not supported. ", step.id));
      }
      if (this.isTest && this.stepIds.contains(step.id)) {
        stepRunner.setReader();
      }
      stepRunnerMap.put(step.id, stepRunner);
    }
    return MutablePair.of(stepRunnerMap, contexts);
  }

  private StepRunner mlStep(
      Map<String, StepRunner> stepRunnerMap,
      Stack<PipelineContext> contexts,
      Step mlstep,
      Set<Step> predecessors) {
    // TODO: entable it when ml is supported, rethink about how to handle ml steps
    return null;
    /*
    StepRunner stepRunner;
    Set<MutablePair<Step, Schema>> mlSources = new HashSet<>();

    if (IsStepTrainingMode.of().filter(mlstep)) {
      // Flush Data
      for (Step s : predecessors) {
        MutablePair<Step, Schema> stepSchemaMutablePair = flushData(s.id, stepRunnerMap.get(s.id));
        mlSources.add(stepSchemaMutablePair);
      }
      // start a new context here for ML batching
      PipelineContext mlCtxt =
          PipelineContext.of(null, StepBuilder.createPipelineRunner(this.isTest, this.engine));
      contexts.push(mlCtxt);
    }
    StepRunner previous = null;
    for (Step step : predecessors) {
      if (!IsStepTrainingMode.of().filter(step)) {
        previous = stepRunnerMap.get(step.id);
      }
    }
    stepRunner =
        StepBuilder.mlTransformer(mlstep, contexts.peek(), previous, this.isTest, mlSources);
    return stepRunner;
    */
  }

  public MutablePair<Step, Schema> flushData(String stepId, StepRunner stepRunner) {
    // TODO: enable this when ml function is on live
    return MutablePair.of(new Step(), stepRunner.getSchema());
    /*
    if (stepRunner.getType() == StepType.Loader) {
      throw new IllegalStateException(
          String.format("Unexpected Step %s . Should not be a destination.", stepId));
    }
    Step step = new Step();
    step.type = StepConfig.TYPE_LOADER;
    step.id = stepId;
    step.config = new HashMap<>();
    step.config.put(StepConfig.SUBTYPE, "file");
    step.config.put(StepConfig.FORMAT, "csv");
    step.config.put(StepConfig.PATH, HomeManager.getTempDirectory());
    step.config.put(StepConfig.PREFIX, stepId);
    step.config.put(StepConfig.IS_HEADER, false);
    step.config.put(StepConfig.SHARD, "1");

    Iterable<Step> predecessors = this.dag.DFSBranch(stepId);
    Iterable<Step> cachedPredecessors = stepsCache.getIfPresent(stepId);

    if (cachedPredecessors == null || !Step.deepEquals(cachedPredecessors, predecessors)) {
      // if no cache or predecessors has changed (comparing them to cached predecessors )
      StepBuilder.append(step, stepRunner, false);
      stepsCache.put(stepId, predecessors); // cache it for next execution
    }
    return MutablePair.of(step, stepRunner.getSchema());
    */
  }
}
