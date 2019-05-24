package org.ananas.runner.model.steps.commons.test;

import java.util.Map;
import org.ananas.runner.model.core.Dataframe;
import org.ananas.runner.model.steps.commons.build.Builder;

public class BeamTester implements Tester {

  @Override
  public Map<String, Dataframe> runTest(Builder p) {
    return p.test();
  }
}
