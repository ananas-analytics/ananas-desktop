package org.ananas.runner.model.steps.commons.test;

import java.util.Map;
import org.ananas.runner.model.core.Dataframe;
import org.ananas.runner.model.steps.commons.build.Builder;

public interface Tester {

  Map<String, Dataframe> runTest(Builder p);
}
