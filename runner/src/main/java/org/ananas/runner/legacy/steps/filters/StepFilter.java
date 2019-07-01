package org.ananas.runner.legacy.steps.filters;

import org.ananas.runner.kernel.model.Step;

public interface StepFilter {

  boolean filter(Step step);
}
