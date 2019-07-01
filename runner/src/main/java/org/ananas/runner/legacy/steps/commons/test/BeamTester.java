package org.ananas.runner.legacy.steps.commons.test;

import java.util.Map;
import org.ananas.runner.kernel.build.Builder;
import org.ananas.runner.kernel.model.Dataframe;

public class BeamTester implements Tester {

  @Override
  public Map<String, Dataframe> runTest(Builder p) {
    return p.test();
  }
}
