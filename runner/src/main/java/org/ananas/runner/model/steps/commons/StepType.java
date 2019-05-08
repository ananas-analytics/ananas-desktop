package org.ananas.runner.model.steps.commons;

/**
 * Enumeration of StepType 
 */
public enum StepType {
	Connector, Transformer, Viewer, Loader;

	public static StepType from(String v) {
		for (StepType t : StepType.values()) {
			if (t.name().toLowerCase().equals(v.toLowerCase())) {
				return t;
			}
		}
		throw new RuntimeException("cannot find step type for value " + v);
	}
}
