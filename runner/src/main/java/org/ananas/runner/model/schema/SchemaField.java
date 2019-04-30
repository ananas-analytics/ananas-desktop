package org.ananas.runner.model.schema;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.apache.beam.sdk.extensions.sql.impl.utils.CalciteUtils;
import org.apache.beam.sdk.schemas.Schema;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SchemaField {
	public int index;
	public String name;
	public String type;
	public boolean userDefined;
	public org.ananas.runner.model.schema.Schema rowSchema;
	public SchemaField arrayElementType;

	public static SchemaField Of(int index, String fieldName, Schema.FieldType type) {
		SchemaField o = new SchemaField();
		o.index = index;
		o.name = fieldName;
		o.type = convert(type);
		o.userDefined = false;
		o.rowSchema = null;
		o.arrayElementType = null;

		if (type.getTypeName().isCompositeType()) {
			o.rowSchema = org.ananas.runner.model.schema.Schema.Of(type.getRowSchema());
		} else if (type.getTypeName().isCollectionType() && type.getCollectionElementType() != null) {
			o.arrayElementType = SchemaField.Of(1, "", type.getCollectionElementType());
		}
		return o;
	}

	public static String convert(Schema.FieldType type) {
		return CalciteUtils.toSqlTypeName(type).getName();
	}


}
