package org.ananas.runner.steprunner.jdbc.derby;

import org.ananas.runner.steprunner.jdbc.JDBCDriver;
import org.ananas.runner.steprunner.jdbc.SQLDialect;
import org.apache.beam.sdk.schemas.Schema;

public class DerbySQLDialect implements SQLDialect {

  private DerbySQLDialect() {}

  public static SQLDialect of() {
    return new DerbySQLDialect();
  }

  @Override
  public String addColumnStatement(JDBCDriver driver, String tablename, Schema.Field n) {
    return String.format(
        "ALTER TABLE %s ADD COLUMN %s %s",
        tablename, n.getName(), driver.getDefaultDataType(n.getType()).getDatatypeLiteral());
  }

  @Override
  public String dropExistingColumnStatement(String tablename, Schema.Field f) {
    return String.format("ALTER TABLE %s DROP COLUMN %s", tablename, f.getName());
  }

  @Override
  public String dropTableStatement(String tablename) {
    StringBuilder builder = new StringBuilder();
    builder.append("DROP TABLE ");
    builder.append(tablename);
    builder.append(" ");
    return builder.toString();
  }

  @Override
  public String createTableStatement(JDBCDriver driver, String tablename, Schema schema) {
    StringBuilder builder = new StringBuilder();
    builder.append("CREATE TABLE ");
    builder.append(tablename);
    builder.append(" ( ");
    for (int i = 0; i < schema.getFields().size(); i++) {
      String fieldName = schema.getField(i).getName();
      builder.append(fieldName);
      builder.append(" ");
      builder.append(driver.getDefaultDataType(schema.getField(i).getType()).getDatatypeLiteral());
      if (i != schema.getFields().size() - 1) {
        builder.append(",");
      }
    }
    builder.append(")");
    return builder.toString();
  }

  @Override
  public String insertStatement(String tablename, Schema schema) {
    StringBuilder builder = new StringBuilder();
    builder.append("insert into ");
    builder.append(tablename);
    builder.append(" values (");
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
    return ""; // TODO " OFFSET 10 ROWS FETCH NEXT 100 ROWS ONLY ";
  }
}
