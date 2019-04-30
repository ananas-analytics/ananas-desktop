package org.ananas.runner.model.steps.db.jdbc.mysql;

import org.ananas.runner.model.steps.db.jdbc.JDBCDriver;
import org.ananas.runner.model.steps.db.jdbc.SQLDialect;
import org.apache.beam.sdk.schemas.Schema;

public class MySQLDialect implements SQLDialect {

	private MySQLDialect() {
	}

	public static SQLDialect of() {
		return new MySQLDialect();
	}

	@Override
	public String addColumnStatement(JDBCDriver driver, String tablename, Schema.Field n) {
		return String.format("ALTER TABLE %s ADD COLUMN %s %s;", tablename, n.getName(),
				driver.getDefaultDataType(n.getType()).getDatatypeLiteral());
	}

	@Override
	public String dropExistingColumnStatement(String tablename, Schema.Field f) {
		return String.format("ALTER TABLE %s RENAME COLUMN %s TO old_%s;", tablename, f.getName());
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
		builder.append("CREATE TABLE ");
		builder.append(tablename);
		builder.append(" ( ");
		for (int i = 0; i < schema.getFields().size(); i++) {
			builder.append(schema.getField(i).getName());
			builder.append(" ");
			builder.append(driver.getDefaultDataType(schema.getField(i).getType()).getDatatypeLiteral());
			builder.append(" NULL DEFAULT NULL ");
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
