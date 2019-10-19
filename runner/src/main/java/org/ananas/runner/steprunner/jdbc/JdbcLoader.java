package org.ananas.runner.steprunner.jdbc;

import avro.shaded.com.google.common.base.Preconditions;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import org.ananas.runner.core.LoaderStepRunner;
import org.ananas.runner.core.StepRunner;
import org.ananas.runner.core.common.DataReader;
import org.ananas.runner.core.model.Step;
import org.ananas.runner.misc.NullDataReader;
import org.apache.beam.sdk.io.jdbc.JdbcIO;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.schemas.Schema.Field;
import org.apache.beam.sdk.schemas.SchemaCoder;
import org.apache.beam.sdk.values.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcLoader extends LoaderStepRunner {

  private static final Logger LOG = LoggerFactory.getLogger(JdbcLoader.class);
  private static final long serialVersionUID = -8461806107228452027L;

  public JdbcLoader(Step step, StepRunner previous, boolean isTest) {
    super(step, previous, isTest);
  }

  @Override
  public DataReader getReader() {
    return NullDataReader.of();
  }

  @Override
  public Schema getSchema() {
    return Schema.builder().build();
  }

  @Override
  public void setReader() {
    // NO OPER
  }

  @Override
  public void build() {
    JdbcStepConfig jdbcConfig = new JdbcStepConfig(step.config);
    String tablename = (String) step.config.get(JdbcStepConfig.JDBC_TABLENAME);
    Boolean overwrite = (Boolean) step.config.getOrDefault(JdbcStepConfig.JDBC_OVERWRITE, true);

    super.output = null;

    Preconditions.checkNotNull(tablename, "tablename cannot be empty");
    validateSQLName(tablename, "table name");
    Schema schema = ((SchemaCoder) previous.getOutput().getCoder()).getSchema();

    for (int i = 0; i < schema.getFields().size(); i++) {
      JDBCDataType dataType = jdbcConfig.driver.getDefaultDataType(schema.getField(i).getType());
      if (dataType == null) {
        throw new RuntimeException(
            String.format(
                schema.getField(i).getName() + " field type [%s] is not supported for %s",
                schema.getField(i).getType().getTypeName(),
                jdbcConfig.driver.driverName
                    + ". Please use a transformer to exclude this column."));
      }
      validateSQLName(schema.getField(i).getName(), "field name");
    }

    if (isTest) {
      // test connection
      JDBCStatement.Execute(
          jdbcConfig.driver,
          jdbcConfig.url,
          jdbcConfig.username,
          jdbcConfig.password,
          (conn, statement) -> {
            return null;
          });
      jdbcConfig.driver.SQLDialect().createTableStatement(jdbcConfig.driver, tablename, schema);
      return;
    }

    migrateTable(
        overwrite,
        tablename,
        jdbcConfig.driver,
        jdbcConfig.url,
        jdbcConfig.username,
        jdbcConfig.password,
        schema);
    JdbcIO.DataSourceConfiguration jdbcConfiguration =
        JdbcIO.DataSourceConfiguration.create(
            jdbcConfig.driver.driverClassName, jdbcConfig.driver.ddl.rewrite(jdbcConfig.url));

    if (jdbcConfig.driver != JDBCDriver.DERBY) {
      jdbcConfiguration =
          jdbcConfiguration.withUsername(jdbcConfig.username).withPassword(jdbcConfig.password);
    }

    previous
        .getOutput()
        .apply(
            JdbcIO.<Row>write()
                .withDataSourceConfiguration(jdbcConfiguration)
                .withStatement(jdbcConfig.driver.SQLDialect().insertStatement(tablename, schema))
                .withPreparedStatementSetter(
                    new JdbcIO.PreparedStatementSetter<Row>() {
                      private static final long serialVersionUID = 4709687496583896251L;

                      @Override
                      public void setParameters(Row element, PreparedStatement query)
                          throws SQLException {
                        for (int i = 0; i < schema.getFields().size(); i++) {
                          jdbcConfig.driver.setParameter(
                              i + 1, schema.getField(i).getType(), query, element.getValue(i));
                        }
                      }
                    }));
  }

  private void validateSQLName(String name, String prefix) {
    if (!name.matches("[\\w_\\d]+")) {
      throw new RuntimeException(
          String.format(
              prefix + " [%s] is not a valid SQL name. Please use a transformer to rename it.",
              name));
    }
  }

  /**
   * Migrate table. It takes care of recreating table or updating columns if necessary
   *
   * @param overwrite Do you want to overwrite the target table or append data ?
   * @param tablename
   * @param driver we neeed this driver to execute SQL Statements.
   * @param url
   * @param username
   * @param password
   * @param schema the schemas of input collection rows
   */
  private static void migrateTable(
      boolean overwrite,
      String tablename,
      JDBCDriver driver,
      String url,
      String username,
      String password,
      Schema schema) {
    // try to create it
    try {
      JDBCStatement.Execute(
          driver,
          url,
          username,
          password,
          (conn, statement) -> {
            String s = driver.SQLDialect().createTableStatement(driver, tablename, schema);
            LOG.debug("Create table statement : " + s);
            statement.executeUpdate(s);
            return null;
          });
    } catch (Exception e) {
      if (overwrite) {
        // Recreate it
        try {
          JDBCStatement.Execute(
              driver,
              url,
              username,
              password,
              (conn, statement) -> {
                String s = driver.SQLDialect().dropTableStatement(tablename);
                LOG.debug("drop table statement : " + s);
                statement.executeUpdate(s);
                return null;
              });
        } catch (Exception e2) {
        }
        JDBCStatement.Execute(
            driver,
            url,
            username,
            password,
            (conn, statement) -> {
              String s = driver.SQLDialect().createTableStatement(driver, tablename, schema);
              LOG.debug("create table statement : " + s);
              statement.executeUpdate(s);
              return null;
            });
      } else {
        migrateTableIfExists(overwrite, tablename, driver, url, username, password, schema);
      }
    }
  }

  private static void migrateTableIfExists(
      boolean overwrite,
      String tablename,
      JDBCDriver driver,
      String url,
      String username,
      String password,
      Schema schema) {

    // compare schemas . If new column add them. If same column with different types, add the column
    // and rename the old one.
    Schema oldSchema =
        JdbcSchemaDetecter.autodetect(
            driver, url, username, password, String.format("SELECT * FROM %s", tablename));

    for (int i = 0; i < oldSchema.getFields().size(); i++) {
      Schema.Field n = oldSchema.getField(i);
      Optional<Field> f =
          schema.getFields().stream()
              .filter(field -> field.getName().equalsIgnoreCase(n.getName()))
              .findFirst();

      if (!f.isPresent()) {
        // should remove it here
        JDBCStatement.Execute(
            driver,
            url,
            username,
            password,
            (conn, statement) -> {
              String s = driver.SQLDialect().dropExistingColumnStatement(tablename, n);
              LOG.debug("Drop column statement : " + s);
              statement.executeUpdate(s);
              return null;
            });
      }
    }

    for (int i = 0; i < schema.getFields().size(); i++) {
      Schema.Field n = schema.getField(i);
      Optional<Schema.Field> f =
          oldSchema.getFields().stream()
              .filter(field -> field.getName().equalsIgnoreCase(n.getName()))
              .findFirst();

      if (!f.isPresent()) {
        // should add it here
        JDBCStatement.Execute(
            driver,
            url,
            username,
            password,
            (conn, statement) -> {
              String s = driver.SQLDialect().addColumnStatement(driver, tablename, n);
              LOG.debug("Add column statement : " + s);
              statement.executeUpdate(s);
              return null;
            });
      } else if (f.get().getType().equals(n.getType())) {
        // not same columns -> check if compatible type
        // we need to drop OLD COLUMNS AND ADD THIS NEW COLUMN.
        JDBCStatement.Execute(
            driver,
            url,
            username,
            password,
            (conn, statement) -> {
              String s = driver.SQLDialect().dropExistingColumnStatement(tablename, n);
              LOG.debug("Drop column statement : " + s);
              statement.executeUpdate(s);
              return null;
            });
        JDBCStatement.Execute(
            driver,
            url,
            username,
            password,
            (conn, statement) -> {
              String s = driver.SQLDialect().addColumnStatement(driver, tablename, n);
              LOG.debug("Add column statement : " + s);
              statement.executeUpdate(s);
              return null;
            });
      }
    }
  }
}
