package org.ananas.runner.model.steps.commons.build;

import org.ananas.runner.model.core.Dataframe;
import org.ananas.runner.model.core.PipelineContext;
import org.ananas.runner.model.steps.commons.StepRunner;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.Map;
import java.util.Set;
import java.util.Stack;

public interface Builder {

	MutablePair<Map<String, StepRunner>, Stack<PipelineContext>> build();

	Map<String, Dataframe> test();

	// get the goals
	Set<String> getGoals();
}
