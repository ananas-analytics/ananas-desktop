package org.ananas.runner.model.steps.db.jdbc;

import org.apache.beam.sdk.schemas.Schema;

public interface DDL {
	JDBCDataType getDefaultDataType(Schema.FieldType type);

	Schema.FieldType getDefaultDataTypeLiteral(String datatypeLiteral);

	String rewrite(String url);
}
