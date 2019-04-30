package org.ananas.runner.model.core.dag;

import com.google.common.base.Preconditions;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.google.common.graph.Traverser;
import org.ananas.runner.model.core.Dag;
import org.ananas.runner.model.core.Step;
import org.ananas.runner.model.errors.DatumaniaException;
import org.ananas.runner.model.errors.ExceptionHandler;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DatumaniaGraph {

	Map<String, Step> stepdIds;
	MutableGraph<Step> graph;
	Dag dag;
	Set<String> goals;

	public DatumaniaGraph(Dag dag, Set<String> goals) {
		this.dag = dag;
		this.goals = goals;
		this.stepdIds = new HashMap<>();
		this.build();
	}

	/**
	 * Builds graph
	 */
	private void build() {
		this.graph = GraphBuilder.directed().build();
		for (Step step : this.dag.getSteps()) {
			this.graph.addNode(step);
			this.stepdIds.put(step.id, step);
		}
		for (Dag.DagEdge e : this.dag.getConnections()) {
			Step stepSource = this.stepdIds.get(e.source);
			Preconditions.checkNotNull(stepSource, String.format("Oops cannot find step for source id %s", e.source));
			Step stepTarget = this.stepdIds.get(e.target);
			Preconditions.checkNotNull(stepTarget, String.format("Oops cannot find step for target id %s", e.target));
			this.graph.putEdge(stepSource, stepTarget);
		}
		if (com.google.common.graph.Graphs.hasCycle(this.graph)) {
			throw new DatumaniaException(
					MutablePair.of(ExceptionHandler.ErrorCode.DAG,
							"Oops. Your DAG has a cycle ie a sequence of connections starting and ending with the same step."));
		}
	}

	public DatumaniaGraph reverse() {
		Dag copy = this.dag.copy();
		Set<Dag.DagEdge> edges = new HashSet<>();
		for (Dag.DagEdge edge : copy.getConnections()) {
			edges.add(new Dag.DagEdge(edge.target, edge.source));
		}
		copy.setConnections(edges);
		DatumaniaGraph dagGraphBuilder = new DatumaniaGraph(copy, this.goals);
		return dagGraphBuilder;
	}

	public Map<String, Iterable<Step>> DFSBranch(Set<String> leaves) {
		Map<String, Iterable<Step>> m = new HashMap<>();
		for (String stepId : leaves) {
			m.put(stepId, DFSBranch(stepId));
		}
		return m;
	}

	public Iterable<Step> DFSBranch(String stepId) {
		return Traverser.forGraph(this.graph).depthFirstPostOrder(
				this.stepdIds.get(stepId));
	}

	public Map<String, Iterable<Step>> DFSBranch() {
		return DFSBranch(this.goals);
	}

	public DatumaniaGraph subDag(Set<String> leaves) {

		Map<String, Iterable<Step>> m = DFSBranch(leaves);
		Set<Step> set = new HashSet<>();
		for (Map.Entry<String, Iterable<Step>> e : m.entrySet()) {
			for (Step s : e.getValue()) {
				set.add(s);
			}
		}

		Set<Dag.DagEdge> connections = new HashSet();
		for (Step s : set) {
			for (Step successor : this.graph.successors(s)) {
				connections.add(new Dag.DagEdge(s.id, successor.id));
			}
		}

		Dag subDag = new Dag(connections, set);
		return new DatumaniaGraph(subDag, leaves);
	}

	public Set<Step> topologicalSort() {
		return Graphs.topologicallySortedNodes(this.graph);
	}


	public Set<Step> successors(Step step) {
		return this.graph.successors(step);
	}

	public boolean isStartNode(Step step) {
		for (EndpointPair<Step> edge : this.graph.incidentEdges(step)) {
			if (edge.target().equals(step)) {
				return false;
			}
		}
		return true;
	}

	public Set<Step> predecessors(Step step) {
		Set<Step> steps = new HashSet<>();
		for (EndpointPair<Step> edge : this.graph.incidentEdges(step)) {
			if (edge.target().equals(step)) {
				steps.add(edge.source());
			}
		}
		return steps;
	}

	@Override
	public String toString() {
		return "DatumaniaGraph{" +
				"stepdIds=" + this.stepdIds +
				", graph=" + this.graph +
				", dag=" + this.dag +
				'}';
	}
}
