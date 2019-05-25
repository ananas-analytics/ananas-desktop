package org.ananas.runner.model.steps.dataview;

import org.ananas.runner.kernel.StepRunner;
import org.ananas.runner.model.steps.db.JdbcLoader;
import org.ananas.runner.steprunner.jdbc.JDBCDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A Data View loader into DERBY table . A special case of JDBCLoader. */
public class DataViewLoader extends JdbcLoader {

  private static final Logger LOG = LoggerFactory.getLogger(DataViewLoader.class);
  private static final long serialVersionUID = 5216574296326021692L;

  public DataViewLoader(String stepId, StepRunner previous, boolean isTest) {
    super(
        stepId,
        true,
        "table_" + stepId,
        JDBCDriver.DERBY,
        DataViewRepository.URL(true),
        null,
        null,
        previous,
        isTest);
    LOG.debug("Dataview loading - table " + stepId);
  }
}
