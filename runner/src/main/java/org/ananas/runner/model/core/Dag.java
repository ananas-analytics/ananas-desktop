package org.ananas.runner.model.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Dag {
	public Set<DagEdge> connections;
	public Set<Step> steps;

	@Override
	public String toString() {
		return "Dag{" +
				"connections=" + this.connections +
				", steps=" + this.steps +
				'}';
	}

	public void setConnections(Set<DagEdge> connections) {
		this.connections = connections;
	}

	public static class DagEdge {

		public String source;
		public String target;

		public DagEdge(String source, String target) {
			this.source = source;
			this.target = target;
		}

		public DagEdge() {
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof DagEdge)) {
				return false;
			}

			DagEdge dagEdge = (DagEdge) o;

			if (this.source != null ? !this.source.equals(dagEdge.source) : dagEdge.source != null) {
				return false;
			}
			return this.target != null ? this.target.equals(dagEdge.target) : dagEdge.target == null;
		}

		@Override
		public int hashCode() {
			int result = this.source != null ? this.source.hashCode() : 0;
			result = 31 * result + (this.target != null ? this.target.hashCode() : 0);
			return result;
		}

		@Override
		public String toString() {
			return "DagEdge{" +
					"source='" + this.source + '\'' +
					", target='" + this.target + '\'' +
					'}';
		}
	}

	public Dag(Set<DagEdge> connections, Set<Step> steps) {
		this.connections = connections;
		this.steps = steps;
	}

	public Dag copy() {
		return new Dag(new HashSet<>(this.connections), new HashSet<>(this.steps));
	}

	public Dag() {
		this.connections = new HashSet<>();
		this.steps = new HashSet<>();
	}

	public Set<DagEdge> getConnections() {
		return this.connections;
	}

	public Set<Step> getSteps() {
		return this.steps;
	}

}