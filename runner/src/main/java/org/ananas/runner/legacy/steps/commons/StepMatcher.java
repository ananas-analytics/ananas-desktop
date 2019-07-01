package org.ananas.runner.legacy.steps.commons;

import org.ananas.runner.kernel.StepRunner;

public interface StepMatcher {
  boolean match(StepRunner i);
}
