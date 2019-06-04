package org.ananas.runner.steprunner;

import java.util.HashMap;
import java.util.Map;
import org.ananas.runner.kernel.DataViewerStepRunner;
import org.ananas.runner.kernel.StepRunner;
import org.ananas.runner.kernel.model.Dataframe;
import org.ananas.runner.kernel.model.Engine;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.kernel.model.StepType;
import org.ananas.runner.kernel.paginate.Paginator;
import org.ananas.runner.model.steps.commons.paginate.SourcePaginator;
import org.ananas.runner.steprunner.files.utils.HomeManager;
import org.ananas.runner.steprunner.jdbc.JDBCDriver;
import org.ananas.runner.steprunner.jdbc.JdbcLoader;
import org.ananas.runner.steprunner.jdbc.JdbcStepConfig;
import org.ananas.runner.steprunner.sql.SQLTransformer;

public class DefaultDataViewer extends DataViewerStepRunner {

  private static final long serialVersionUID = 4331603846982823797L;

  public DefaultDataViewer(Engine engine, Step step, StepRunner previous, boolean isTest) {
    super(engine, step, previous, isTest);
  }

  public void build() {
    // DefaultDataViewer is actually a wrapper of SQLTransformer in test mode and a jdbcLoader in
    // run mode
    // don't forget to copy the delegation state to the wrapper. (output, for example)
    if (isTest) {
      StepRunner sqlQuery = new SQLTransformer(step, previous);
      sqlQuery.build();
      this.output = sqlQuery.getOutput();
    } else {
      // TODO: accept these configurations from settings
      step.config.put(JdbcStepConfig.JDBC_OVERWRITE, true);
      step.config.put(JdbcStepConfig.JDBC_TABLENAME, "table_" + step.id);
      step.config.put(JdbcStepConfig.JDBC_TYPE, this.getJdbcType());
      step.config.put(JdbcStepConfig.JDBC_URL, this.getJdbcURL());
      step.config.put(JdbcStepConfig.JDBC_USER, this.getJdbcUser());
      step.config.put(JdbcStepConfig.JDBC_PASSWORD, this.getJdbcPassword());

      StepRunner jdbcLoader = new JdbcLoader(step, previous, isTest);
      jdbcLoader.build();
      this.output = jdbcLoader.getOutput();
    }
  }

  private String getJdbcType() {
    String type = engine.getProperty(Engine.VIEW_DB_TYPE, "derby");
    return type;
  }

  private String getJdbcURL() {
    return engine.getProperty(Engine.VIEW_DB_URL, DataViewRepository.URL(false));
  }

  private String getJdbcUser() {
    String user =  engine.getProperty(Engine.VIEW_DB_USER, "");
    if ("".equals(user)) {
      return null;
    }
    return user;
  }

  private String getJdbcPassword() {
    String password =  engine.getProperty(Engine.VIEW_DB_PASSWORD, "");
    if ("".equals(password)) {
      return null;
    }
    return password;
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
      config.put(JdbcStepConfig.JDBC_TYPE, JDBCDriver.DERBY.driverName);
      config.put(JdbcStepConfig.JDBC_SQL, s);
      config.put(JdbcStepConfig.JDBC_URL, URL(false));
      config.put(JdbcStepConfig.JDBC_TABLENAME, tName);
      config.put(JdbcStepConfig.JDBC_OVERWRITE, true);
      config.put(JdbcStepConfig.JDBC_USER, "");
      config.put(JdbcStepConfig.JDBC_PASSWORD, "");

      Paginator paginator =
          SourcePaginator.of(tName, StepType.Connector.name(), config, new HashMap<>());
      Dataframe dataframe = paginator.paginate(0, Integer.MAX_VALUE);
      return dataframe;
    }
  }
}
