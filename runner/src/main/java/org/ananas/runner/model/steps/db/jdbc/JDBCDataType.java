package org.ananas.runner.model.steps.db.jdbc;

import org.apache.beam.sdk.schemas.Schema;

public interface JDBCDataType {

	/**
	 * Default type used for DDL script. When true, we use this type when the field type matches.
	 *
	 * @return
	 */
	boolean isDefault();

	Schema.FieldType getFieldType();

	String getDatatypeLiteral();

}
