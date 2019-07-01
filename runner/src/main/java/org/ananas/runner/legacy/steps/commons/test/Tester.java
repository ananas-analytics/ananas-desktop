package org.ananas.runner.legacy.steps.commons.test;

import java.util.Map;
import org.ananas.runner.kernel.build.Builder;
import org.ananas.runner.kernel.model.Dataframe;

public interface Tester {

  Map<String, Dataframe> runTest(Builder p);
}
