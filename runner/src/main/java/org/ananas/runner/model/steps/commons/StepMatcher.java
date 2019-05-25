package org.ananas.runner.model.steps.commons;

import org.ananas.runner.kernel.StepRunner;

public interface StepMatcher {
  boolean match(StepRunner i);
}
