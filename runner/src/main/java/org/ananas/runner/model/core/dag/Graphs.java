package org.ananas.runner.model.core.dag;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.graph.Graph;

import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toMap;


/**
 * Graph algorithms
 */
public class Graphs<N> {

	public static <N> Set<N> topologicallySortedNodes(Graph<N> graph) {
		return new TopologicallySortedNodes<>(graph);
	}

	private static class TopologicallySortedNodes<N> extends AbstractSet<N> {
		private final Graph<N> graph;

		private TopologicallySortedNodes(Graph<N> graph) {
			this.graph = checkNotNull(graph, "graph");
		}

		@Override
		public UnmodifiableIterator<N> iterator() {
			return new TopologicalOrderIterator<>(this.graph);
		}

		@Override
		public int size() {
			return this.graph.nodes().size();
		}

		@Override
		public boolean remove(Object o) {
			throw new UnsupportedOperationException();
		}
	}

	private static class TopologicalOrderIterator<N> extends AbstractIterator<N> {
		private final Graph<N> graph;
		private final Queue<N> roots;
		private final Map<N, Integer> nonRootsToInDegree;

		private TopologicalOrderIterator(Graph<N> graph) {
			this.graph = checkNotNull(graph, "graph");
			this.roots =
					graph
							.nodes()
							.stream()
							.filter(node -> graph.inDegree(node) == 0)
							.collect(toCollection(ArrayDeque::new));
			this.nonRootsToInDegree =
					graph
							.nodes()
							.stream()
							.filter(node -> graph.inDegree(node) > 0)
							.collect(toMap(node -> node, graph::inDegree, (a, b) -> a, HashMap::new));
		}

		@Override
		protected N computeNext() {
			// Kahn's algorithm
			if (!this.roots.isEmpty()) {
				N next = this.roots.remove();
				for (N successor : this.graph.successors(next)) {
					int newInDegree = this.nonRootsToInDegree.get(successor) - 1;
					this.nonRootsToInDegree.put(successor, newInDegree);
					if (newInDegree == 0) {
						this.nonRootsToInDegree.remove(successor);
						this.roots.add(successor);
					}
				}
				return next;
			}
			checkState(this.nonRootsToInDegree.isEmpty(), "graph has at least one cycle");
			return endOfData();
		}
	}

}
