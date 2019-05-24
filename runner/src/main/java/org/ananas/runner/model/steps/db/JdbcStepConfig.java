package org.ananas.runner.model.steps.db;

import java.util.Map;
import org.ananas.runner.model.core.StepConfig;
import org.ananas.runner.model.steps.db.jdbc.JDBCDriver;

public class JdbcStepConfig {

  public JDBCDriver driver;
  public String url;
  public String username;
  public String password;
  public String sql;
  public JDBCDriver sqlDialect;

  public JdbcStepConfig(Map<String, Object> config) {
    this.driver = JDBCDriver.NONE.getDriverByName((String) config.get(StepConfig.JDBC_TYPE));
    this.sqlDialect =
        JDBCDriver.NONE.getDriverByName(
            (String) config.getOrDefault(StepConfig.JDBC_SQL_DIALECT_TYPE, this.driver.driverName));
    this.url = "jdbc:" + (String) config.get(StepConfig.JDBC_URL);
    this.username = (String) config.get(StepConfig.JDBC_USER);
    this.password = (String) config.get(StepConfig.JDBC_PASSWORD);
    this.sql = (String) config.get(StepConfig.JDBC_SQL);
  }
}
