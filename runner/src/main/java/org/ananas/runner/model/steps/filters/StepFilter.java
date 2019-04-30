package org.ananas.runner.model.steps.filters;

import org.ananas.runner.model.core.Step;

public interface StepFilter {

	boolean filter(Step step);
}
