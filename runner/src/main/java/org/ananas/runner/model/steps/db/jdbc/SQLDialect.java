package org.ananas.runner.model.steps.db.jdbc;

import org.apache.beam.sdk.schemas.Schema;

public interface SQLDialect {

	/**
	 * Adds a ne column to existing table
	 *
	 * @param driver    the JDBC driver
	 * @param tablename the table name
	 * @param n         the field corresponding to the column to be added
	 * @return the SQL Statement that adds the column
	 */
	String addColumnStatement(JDBCDriver driver, String tablename, Schema.Field n);

	/**
	 * Rename if possible or drop an existing column
	 *
	 * @param tablename the table name
	 * @param f         the field corresponding to the column to be added
	 * @return the SQL Statement that renames the column
	 */
	String dropExistingColumnStatement(String tablename, Schema.Field f);

	/**
	 * Drop the table if it exists
	 *
	 * @param tablename the table name
	 * @return the SQL statement that drop the table
	 */
	String dropTableStatement(String tablename);

	/**
	 * Create the table
	 *
	 * @param driver    the JDBC driver
	 * @param tablename the table name
	 * @param schema    the schemas required to create table
	 * @return the SQL statement that creates the table
	 */
	String createTableStatement(JDBCDriver driver, String tablename, Schema schema);

	/**
	 * Insert a row
	 *
	 * @param tablename the table name
	 * @param schema    the schemas required to build the statement
	 * @return the SQL statement that insert a ronw
	 */
	String insertStatement(String tablename, Schema schema);


	String limit(int limit);

}
