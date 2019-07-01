package org.ananas.runner.legacy.steps.db;

import java.io.Serializable;
import java.util.Optional;
import org.ananas.runner.kernel.StepRunner;
import org.ananas.runner.legacy.steps.commons.AbstractStepLoader;
import org.ananas.runner.legacy.steps.commons.json.AsBsons;
import org.ananas.runner.steprunner.jdbc.JDBCDriver;
import org.ananas.runner.steprunner.jdbc.JDBCStatement;
import org.ananas.runner.steprunner.jdbc.JdbcSchemaDetecter;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoDBLoader extends AbstractStepLoader implements StepRunner, Serializable {

  private static final Logger LOG = LoggerFactory.getLogger(MongoDBLoader.class);
  private static final long serialVersionUID = -5336365297270280769L;

  public MongoDBLoader(
      String stepId,
      String uri,
      String database,
      String collection,
      StepRunner previous,
      boolean isTest) {
    this.stepId = stepId;
    super.output = null;

    if (isTest) {
      return;
    }

    previous
        .getOutput()
        .apply(AsBsons.of())
        .apply(
            org.apache.beam.sdk.io.mongodb.MongoDbIO.<Row>write()
                .withUri(uri)
                .withDatabase(database)
                .withCollection(collection));
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
  public static void migrateTable(
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
      Optional<Schema.Field> f =
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
