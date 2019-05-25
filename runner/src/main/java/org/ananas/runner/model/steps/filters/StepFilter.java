package org.ananas.runner.model.steps.filters;

import org.ananas.runner.kernel.model.Step;

public interface StepFilter {

  boolean filter(Step step);
}
