package org.ananas.runner.core.build;

import com.google.common.base.Preconditions;
import java.util.*;
import org.ananas.runner.core.StepRunner;
import org.ananas.runner.core.model.AnanasGraph;
import org.ananas.runner.core.model.Dag;
import org.ananas.runner.core.model.DagRequest;
import org.ananas.runner.core.model.Dataframe;
import org.ananas.runner.core.model.Engine;
import org.ananas.runner.core.model.Step;
import org.ananas.runner.core.model.TriggerOptions;
import org.ananas.runner.core.model.Variable;
import org.ananas.runner.core.pipeline.NoHook;
import org.ananas.runner.core.pipeline.PipelineContext;
import org.apache.commons.lang3.tuple.MutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DagBuilder implements Builder {

  private static final Logger LOG = LoggerFactory.getLogger(DagBuilder.class);

  private AnanasGraph dag;

  private Dag originalDag;

  private Set<String> stepIds;

  private Engine engine;

  private TriggerOptions trigger;

  // private static Cache<String, Iterable<Step>> stepsCache =
  // CacheBuilder.newBuilder().maximumSize(100).expireAfterWrite(10, TimeUnit.MINUTES).build();

  private boolean isTest;
  private Map<String, Variable> variables;

  public DagBuilder(DagRequest dagRequest, boolean isTest) {
    this(
        dagRequest.dag,
        dagRequest.goals,
        dagRequest.params,
        dagRequest.engine,
        dagRequest.trigger,
        isTest);
  }

  private DagBuilder(
      Dag d,
      Set<String> goals,
      Map<String, Variable> variables,
      Engine engine,
      TriggerOptions trigger,
      boolean isTest) {
    this.originalDag = d;
    this.dag = new AnanasGraph(d, goals).reverse().subDag(goals).reverse();
    LOG.debug(this.dag.toString());
    this.stepIds = goals;
    this.isTest = isTest;
    Preconditions.checkNotNull(variables);
    this.variables = variables == null ? new HashMap<>() : variables;
    this.engine = engine;
    this.trigger = trigger;
  }

  public Set<String> getGoals() {
    return this.stepIds;
  }

  @Override
  public Engine getEngine() {
    return this.engine;
  }

  @Override
  public Dag getDag() {
    return this.originalDag;
  }

  @Override
  public TriggerOptions getTrigger() {
    return this.trigger;
  }

  @Override
  public Map<String, Variable> getParams() {
    return this.variables;
  }

  public Map<String, Dataframe> test() {
    Map<String, Dataframe> p = new HashMap<>();

    // build
    MutablePair<Map<String, StepRunner>, Stack<PipelineContext>> runnableDag = build(null);
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

  public MutablePair<Map<String, StepRunner>, Stack<PipelineContext>> build(String jobId) {
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
                    jobId,
                    new NoHook(),
                    StepBuilder.createPipelineRunner(this.isTest, this.engine));
            contexts.push(ctxt);
          }
          stepRunner =
              StepBuilder.connector(this.engine, step, contexts.peek(), this.isTest, this.isTest);
          stepRunnerMap.put(step.id, stepRunner);
          break;
        case 1:
          // if one predecessor
          stepRunner =
              StepBuilder.append(
                  jobId,
                  this.engine,
                  step,
                  stepRunnerMap.get(predecessors.iterator().next().id),
                  this.isTest);
          break;
        case 2:
          // if two predecessors then join both
          if (step.config.get("subtype").equals("join")) {
            Iterator<Step> it = predecessors.iterator();
            StepRunner one = stepRunnerMap.get(it.next().id);
            StepRunner other = stepRunnerMap.get(it.next().id);
            stepRunner = StepBuilder.join(this.engine, step, one, other);
          } else if (step.config.get("subtype").equals("concat")) {
            Iterator<Step> it = predecessors.iterator();
            StepRunner one = stepRunnerMap.get(it.next().id);
            StepRunner other = stepRunnerMap.get(it.next().id);
            stepRunner = StepBuilder.concat(this.engine, step, Arrays.asList(one, other));
          } else {
            throw new RuntimeException(
                String.format(
                    "Ooops something wrong. A join or union step is required here because there are two predecessors.",
                    step.id));
          }
          break;
        default:
          if (step.config.get("subtype").equals("concat")) {
            Iterator<Step> it = predecessors.iterator();
            List<StepRunner> upstreamRunners = new ArrayList<>();
            StepRunner runner = null;
            while (it.hasNext()) {
              runner = stepRunnerMap.get(it.next().id);
              upstreamRunners.add(runner);
            }
            stepRunner = StepBuilder.concat(this.engine, step, upstreamRunners);
          } else {
            throw new RuntimeException(
                String.format("Step %s has more than 3 predecessors. Not supported. ", step.id));
          }
      }
      if (this.isTest && this.stepIds.contains(step.id)) {
        stepRunner.setReader();
      }
      stepRunnerMap.put(step.id, stepRunner);
    }
    return MutablePair.of(stepRunnerMap, contexts);
  }
}
