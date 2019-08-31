package org.ananas.runner.core.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.ananas.runner.core.errors.AnanasException;
import org.junit.Assert;
import org.junit.Test;

public class AnanasGraphTest {

  @Test
  public void dfsSearch() {
    DagRequest dag = wikiDag();
    AnanasGraph builder = new AnanasGraph(dag.dag, dag.goals).reverse();
    Map<String, Iterable<Step>> x = builder.DFSBranch();
    Assert.assertEquals(
        "should have one branch for each runnable step", dag.goals.size(), x.size());
    for (String id : dag.goals) {
      Assert.assertNotNull(x.get(id));
    }
  }

  @Test
  public void reverse() {
    DagRequest dag = wikiDag();
    AnanasGraph original = new AnanasGraph(dag.dag, dag.goals);
    AnanasGraph reversed = original.reverse();
    Assert.assertEquals(original.dag.connections.size(), reversed.dag.connections.size());
  }

  @Test
  public void subDag() {
    DagRequest dag = wikiDag();
    AnanasGraph subdagGraphBuilder =
        new AnanasGraph(dag.dag, dag.goals).reverse().subDag(dag.goals);
    Assert.assertEquals(7, subdagGraphBuilder.dag.connections.size());
    Assert.assertEquals(7, subdagGraphBuilder.dag.steps.size());
  }

  @Test
  public void topologicalSort() {
    DagRequest dag = wikiDag();
    AnanasGraph builder = new AnanasGraph(dag.dag, dag.goals);
    Set<Step> x = builder.topologicalSort();
    Assert.assertEquals("should have same amout of step", dag.dag.getSteps().size(), x.size());
    System.out.println(x.stream().map(s -> s.id).collect(Collectors.toList()));
  }

  @Test(expected = AnanasException.class)
  public void cyclicGraph() {
    Dag cyclicGraph = new Dag();

    for (int i = 1; i < 4; i++) {
      Step s = getStep(i);
      cyclicGraph.steps.add(s);
    }

    cyclicGraph.getConnections().add(new DagEdge("1", "2"));
    cyclicGraph.getConnections().add(new DagEdge("2", "3"));
    cyclicGraph.getConnections().add(new DagEdge("3", "1"));

    Set<String> goals = new HashSet<>();
    goals.add("2");

    new AnanasGraph(cyclicGraph, goals);
  }

  private DagRequest wikiDag() {
    DagRequest dagRequest = new DagRequest();
    Dag dag = new Dag();

    // Building graph example here : https://en.wikipedia.org/wiki/Topological_sorting
    for (int i = 2; i < 12; i++) {
      Step s = getStep(i);
      dag.steps.add(s);
    }
    dag.getConnections().add(new DagEdge("5", "11"));
    dag.getConnections().add(new DagEdge("7", "11"));
    dag.getConnections().add(new DagEdge("7", "8"));
    dag.getConnections().add(new DagEdge("3", "8"));
    dag.getConnections().add(new DagEdge("3", "10"));
    dag.getConnections().add(new DagEdge("11", "10"));
    dag.getConnections().add(new DagEdge("11", "2"));
    dag.getConnections().add(new DagEdge("11", "9"));
    dag.getConnections().add(new DagEdge("8", "9"));

    dagRequest.goals.add("2");
    dagRequest.goals.add("9");
    dagRequest.dag = dag;

    return dagRequest;
  }

  private Step getStep(int i) {
    Step one = new Step();
    one.id = String.valueOf(i);
    one.config = new HashMap<>();

    one.type = "connector";
    return one;
  }
}
