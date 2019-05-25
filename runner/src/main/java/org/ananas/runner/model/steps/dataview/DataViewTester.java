package org.ananas.runner.model.steps.dataview;

import org.ananas.runner.kernel.StepRunner;
import org.ananas.runner.model.steps.sql.SQLTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A Data View tester is an SQL transformer */
public class DataViewTester extends SQLTransformer {

  private static final Logger LOG = LoggerFactory.getLogger(DataViewTester.class);
  private static final long serialVersionUID = 3953936488742118461L;

  public DataViewTester(String stepId, String statement, StepRunner previous) {
    super(stepId, statement, previous);
  }
}
