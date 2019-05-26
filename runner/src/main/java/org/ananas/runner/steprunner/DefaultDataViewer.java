package org.ananas.runner.steprunner;

import java.util.HashMap;
import java.util.Map;
import org.ananas.runner.kernel.DataViewerStepRunner;
import org.ananas.runner.kernel.StepRunner;
import org.ananas.runner.kernel.common.Paginator;
import org.ananas.runner.kernel.model.Dataframe;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.kernel.model.StepType;
import org.ananas.runner.model.steps.commons.paginate.SourcePaginator;
import org.ananas.runner.steprunner.files.utils.HomeManager;
import org.ananas.runner.steprunner.jdbc.JDBCDriver;
import org.ananas.runner.steprunner.jdbc.JdbcLoader;
import org.ananas.runner.steprunner.sql.SQLTransformer;

public class DefaultDataViewer extends DataViewerStepRunner {

  private static final long serialVersionUID = 4331603846982823797L;

  public DefaultDataViewer(Step step, StepRunner previous, boolean isTest) {
    super(step, previous, isTest);
  }

  public void build() {
    if (isTest) {
      StepRunner sqlQuery = new SQLTransformer(step, previous);
      sqlQuery.build();
    } else {
      // TODO: accept these configurations from settings
      step.config.put(JdbcLoader.JDBC_OVERWRITE, true);
      step.config.put(JdbcLoader.JDBC_TABLENAME, "table_" + step.id);
      step.config.put(JdbcLoader.JDBC_TYPE, JDBCDriver.DERBY.toString());
      step.config.put(JdbcLoader.JDBC_URL, DataViewRepository.URL(true));
      step.config.put(JdbcLoader.JDBC_USER, null);
      step.config.put(JdbcLoader.JDBC_PASSWORD, null);

      StepRunner jdbcLoader = new JdbcLoader(step, previous, isTest);
      jdbcLoader.build();
    }
  }

  public static class DataViewRepository {
    private JDBCDriver driver;

    private static String URL = "derby:%s/dataview;create=true";

    public DataViewRepository() {
      this.driver = JDBCDriver.DERBY;
    }

    static String URL(boolean withPrefix) {
      return String.format((withPrefix ? "jdbc:" : "") + URL, HomeManager.getHome());
    }

    public Dataframe query(String sql, String stepId) {

      String tName = "table_" + stepId;
      String s = sql.replaceFirst("PCOLLECTION", tName);

      Map<String, Object> config = new HashMap<>();

      config.put("subtype", "jdbc");
      config.put(JdbcLoader.JDBC_TYPE, JDBCDriver.DERBY.driverName);
      config.put(JdbcLoader.JDBC_SQL, s);
      config.put(JdbcLoader.JDBC_URL, URL(false));
      config.put(JdbcLoader.JDBC_TABLENAME, tName);
      config.put(JdbcLoader.JDBC_OVERWRITE, true);
      config.put(JdbcLoader.JDBC_USER, "");
      config.put(JdbcLoader.JDBC_PASSWORD, "");

      Paginator paginator =
          SourcePaginator.of(tName, StepType.Connector.name(), config, new HashMap<>());
      Dataframe dataframe = paginator.paginate(0, Integer.MAX_VALUE);
      return dataframe;
    }
  }
}
