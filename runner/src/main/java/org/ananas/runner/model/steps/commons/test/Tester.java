package org.ananas.runner.model.steps.commons.test;

import org.ananas.runner.model.core.Dataframe;
import org.ananas.runner.model.steps.commons.build.Builder;

import java.util.Map;

public interface Tester {

	Map<String, Dataframe> runTest(Builder p);
}
