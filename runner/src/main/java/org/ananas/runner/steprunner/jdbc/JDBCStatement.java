package org.ananas.runner.steprunner.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class JDBCStatement {

  private JDBCStatement() {}

  public interface LambdaConnectionAndStatement<T> {
    T doWith(Connection conn, Statement statement) throws java.sql.SQLException;
  }

  public interface LambdaConnection<T> {
    T doWith(Connection conn) throws java.sql.SQLException;
  }

  public static <T> T Execute(
      JDBCDriver driver,
      String url,
      String username,
      String password,
      LambdaConnectionAndStatement<T> l) {

    Statement stmt = null;
    Connection conn = null;
    try {
      Class driverClass = Class.forName(driver.driverClassName);
      Properties properties = new Properties();
      if (driver.isAuthRequired()) {
        properties.setProperty("user", username);
        properties.setProperty("password", password);
      }
      conn =
          ((java.sql.Driver) driverClass.newInstance())
              .connect(driver.ddl.rewrite(url), properties);
      if (conn == null) {
        throw new RuntimeException(
            "Oops we can't connect to your DB. Please review you connection parameter.");
      }
      stmt = conn.createStatement();
      return l.doWith(conn, stmt);
    } catch (SQLException se) {
      throw new RuntimeException("SQL Error : " + se.getMessage());
    } catch (Exception e) {
      throw new RuntimeException("DatabaseHelper Error : " + e.getMessage());
    } finally {
      try {
        if (stmt != null) {
          stmt.close();
        }
        if (conn != null) {
          conn.close();
        }

      } catch (SQLException se2) {
      }
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException se) {
        se.printStackTrace();
      }
    }
  }
}
