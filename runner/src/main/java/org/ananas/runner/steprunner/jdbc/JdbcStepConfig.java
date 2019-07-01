package org.ananas.runner.steprunner.jdbc;

import java.io.Serializable;
import java.util.Map;

public class JdbcStepConfig implements Serializable {

  private static final long serialVersionUID = -712706421590813143L;

  public static final String JDBC_OVERWRITE = "overwrite";
  public static final String JDBC_TYPE = "database";
  public static final String JDBC_URL = "url";
  public static final String JDBC_USER = "user";
  public static final String JDBC_PASSWORD = "password";
  public static final String JDBC_SQL = "sql";
  public static final String JDBC_SQL_DIALECT_TYPE = "inputDialect";
  public static final String JDBC_TABLENAME = "tablename";

  public JDBCDriver driver;
  public String url;
  public String username;
  public String password;
  public String sql;
  public JDBCDriver sqlDialect;

  public JdbcStepConfig(Map<String, Object> config) {
    this.driver = JDBCDriver.NONE.getDriverByName((String) config.get(JDBC_TYPE));
    this.sqlDialect =
        JDBCDriver.NONE.getDriverByName(
            (String) config.getOrDefault(JDBC_SQL_DIALECT_TYPE, this.driver.driverName));
    this.url = "jdbc:" + (String) config.get(JDBC_URL);
    this.username = (String) config.get(JDBC_USER);
    this.password = (String) config.get(JDBC_PASSWORD);
    this.sql = (String) config.get(JDBC_SQL);
  }
}
