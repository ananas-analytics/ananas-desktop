package org.ananas.runner.steprunner.jdbc.pgsql;

import org.ananas.runner.steprunner.jdbc.JDBCDataType;
import org.ananas.runner.steprunner.jdbc.JDBCDriver;
import org.ananas.runner.steprunner.jdbc.SQLDialect;
import org.apache.beam.sdk.schemas.Schema;

public class PostgresqlDialect implements SQLDialect {

  private PostgresqlDialect() {}

  public static SQLDialect of() {
    return new PostgresqlDialect();
  }

  @Override
  public String addColumnStatement(JDBCDriver driver, String tablename, Schema.Field n) {
    return String.format(
        "ALTER TABLE %s ADD COLUMN %s %s;",
        tablename, n.getName(), driver.getDefaultDataType(n.getType()).getDatatypeLiteral());
  }

  @Override
  public String dropExistingColumnStatement(String tablename, Schema.Field f) {
    return String.format("ALTER TABLE %s RENAME COLUMN %s TO old_%s;", tablename, f.getName());
  }

  public String existsTableStatement(String tablename) {
    return String.format(
        "SELECT 1 FROM pg_catalog.pg_class c WHERE  c.relname = '%s' AND c.relkind = 'r'",
        tablename);
  }

  @Override
  public String dropTableStatement(String tablename) {
    StringBuilder builder = new StringBuilder();
    builder.append("DROP TABLE IF EXISTS ");
    builder.append(tablename);
    builder.append(" ;");
    return builder.toString();
  }

  @Override
  public String createTableStatement(JDBCDriver driver, String tablename, Schema schema) {
    StringBuilder builder = new StringBuilder();
    builder.append("CREATE TABLE IF NOT EXISTS ");
    builder.append(tablename);
    builder.append(" ( ");
    for (int i = 0; i < schema.getFields().size(); i++) {
      builder.append(schema.getField(i).getName());
      builder.append(" ");
      JDBCDataType dataType = driver.getDefaultDataType(schema.getField(i).getType());
      builder.append(dataType.getDatatypeLiteral());
      if (i != schema.getFields().size() - 1) {
        builder.append(",");
      }
    }
    builder.append(");");
    return builder.toString();
  }

  @Override
  public String insertStatement(String tablename, Schema schema) {
    StringBuilder builder = new StringBuilder();
    builder.append("insert into ");
    builder.append(tablename);
    builder.append(" ");
    builder.append("values (");
    for (int i = 0; i < schema.getFields().size(); i++) {
      builder.append("?");
      if (i != schema.getFields().size() - 1) {
        builder.append(",");
      }
    }
    builder.append(")");
    return builder.toString();
  }

  @Override
  public String limit(int limit) {
    return " LIMIT " + limit;
  }
}
