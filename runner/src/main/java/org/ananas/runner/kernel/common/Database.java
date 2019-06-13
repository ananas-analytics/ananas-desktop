package org.ananas.runner.kernel.common;

import java.sql.Connection;
import java.sql.ResultSet;
import org.ananas.runner.steprunner.jdbc.JDBCDriver;
import org.ananas.runner.steprunner.jdbc.JDBCStatement;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.SQLDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Database {
  private static final Logger LOG = LoggerFactory.getLogger(Database.class);

  public static final String SCHEMA = "ANANAS";

  // tables
  public static final String TABLE_JOB = "JOB";
  public static final String TABLE_GOAL = "GOAL";

  private JDBCDriver driver;
  private String url;
  private String username;
  private String password;
  private SQLDialect dialect;

  private DSLContext context;

  private static Database INSTANCE = null;

  private Database(
      JDBCDriver driver, String url, String username, String password, SQLDialect dialect) {
    this.driver = driver;
    this.url = url;
    this.username = username;
    this.password = password;
    this.dialect = dialect;
  }

  public static Database of(
      JDBCDriver driver, String url, String username, String password, SQLDialect dialect) {
    if (INSTANCE == null) {
      INSTANCE = new Database(driver, url, username, password, dialect);
    }
    return INSTANCE;
  }

  public void initiate() {
    JDBCStatement.Execute(
        driver,
        url,
        username,
        password,
        (conn, statement) -> {
          ResultSet rs = conn.getMetaData().getTables(null, SCHEMA, TABLE_JOB, null);
          if (rs.next()) {
            // already exist
            LOG.info("database already initiated, skip initiation");
            return null;
          }
          DSLContext context = getContext(conn);
          context.createSchemaIfNotExists(SCHEMA).execute();

          context.setSchema(SCHEMA).execute();

          // create job table
          context
              .createTable(TABLE_JOB)
              // usually uuid length will be only 36
              .column("ID", SQLDataType.VARCHAR.length(40))
              .column("TRIGGER_ID", SQLDataType.VARCHAR.length(40))
              .column("TRIGGER_TYPE", SQLDataType.VARCHAR.length(10))
              .column("STATE", SQLDataType.VARCHAR.length(20))
              .column("MESSAGE", SQLDataType.VARCHAR.length(1024))
              .column("DAG", SQLDataType.BLOB)
              .column("TRIGGER", SQLDataType.BLOB)
              .column("CREATE_AT", SQLDataType.TIMESTAMP)
              .column("UPDATE_AT", SQLDataType.TIMESTAMP)
              .constraints(DSL.constraint("PK_JOB").primaryKey("ID"))
              .execute();

          // create goal table
          context.createTable(TABLE_GOAL)
            .column("ID", SQLDataType.VARCHAR.length(40))
            .column("JOB_ID", SQLDataType.VARCHAR.length(40))
            .constraints(
              DSL.constraint("PK_GOAL").primaryKey("ID")
              // TODO: add foreign key constraint here
            )
            .execute();

          return null;
        });
  }

  public void clean() {
    execute(context -> {
      context.dropTable(TABLE_GOAL).execute();
      context.dropTable(TABLE_JOB).execute();
      context.dropSchema(SCHEMA).execute();
      return null;
    });
  }

  public <T> T execute(LambdaDSLContext<T> l) {
    return JDBCStatement.Execute(
        driver,
        url,
        username,
        password,
        (conn, statement) -> {
          DSLContext context = getContext(conn);
          context.setSchema(SCHEMA).execute();
          return l.doWith(context);
        });
  }

  private DSLContext getContext(Connection connection) {
    Configuration config = new DefaultConfiguration();
    config.set(connection);
    config.set(new Settings().withRenderSchema(true));
    config.set(this.dialect);
    DSLContext context = DSL.using(config);
    return context;
  }

  public interface LambdaDSLContext<T> {
    T doWith(DSLContext context) throws java.sql.SQLException;
  }
}
