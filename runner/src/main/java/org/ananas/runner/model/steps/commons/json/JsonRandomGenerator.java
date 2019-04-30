package org.ananas.runner.model.steps.commons.json;

import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates a row with random values
 */
public class JsonRandomGenerator implements Serializable {

	private static final long serialVersionUID = 6733972391966883763L;
	private Row.Builder builder;
	private Schema schema;

	private JsonRandomGenerator(Schema schema) {
		this.builder = Row.withSchema(schema);
		this.schema = schema;
	}

	public static JsonRandomGenerator of(org.apache.beam.sdk.schemas.Schema schema) {
		return new JsonRandomGenerator(schema);
	}

	/**
	 * Generate Randow Row
	 *
	 * @return
	 */
	public Object generate() {
		return valueOfRow(this.schema.getFields());
	}

	/**
	 * Object value of af an array.
	 *
	 * @return the serializable value of a field
	 */
	private Object valueOfArrayList(Schema.FieldType elementType) {
		List h = new ArrayList();
		for (int i = 0; i < 5; i++) {
			h.add(valueOfAny(elementType));
		}
		return h;
	}

	/**
	 * Object value of a field. If the field is a row we extract its fields as an array of object value.
	 *
	 * @param fieldType
	 * @return the serializable value of a field
	 */
	private Object valueOfAny(Schema.FieldType fieldType) {

		if (fieldType.getTypeName().isCompositeType()) {
			return valueOfRow(fieldType.getRowSchema().getFields());
		}

		if (fieldType.getTypeName().isCollectionType()) {
			return valueOfArrayList(fieldType.getCollectionElementType());
		}

		return valueOfPrimitive(fieldType);
	}

	//TODO generate random values instad of static values
	private Object valueOfPrimitive(Schema.FieldType type) {
		if (type.getTypeName() == Schema.TypeName.INT32) {
			return new Integer(34);
		}
		if (type.getTypeName() == Schema.TypeName.INT64) {
			return 345l;
		}
		if (type.getTypeName() == Schema.TypeName.DOUBLE) {
			return 345.00d;
		}
		if (type.getTypeName() == Schema.TypeName.BOOLEAN) {
			return false;
		}
		if (type.getTypeName() == Schema.TypeName.DECIMAL) {
			return new BigDecimal("11.11");
		}
		if (type.getTypeName() == Schema.TypeName.FLOAT) {
			return 12345.345f;
		}
		if (type.getTypeName() == Schema.TypeName.BYTE) {
			return 00;
		}
		if (type.getTypeName() == Schema.TypeName.STRING) {
			return "randstring";
		}
		//TODO generate Date
		return null;
	}


	/**
	 * Object value of af a row.
	 *
	 * @param fields any row
	 * @return the serializable value of a field
	 */
	private Map<String, Object> valueOfRow(List<Schema.Field> fields) {
		Map<String, Object> row = new HashMap<>();
		for (Schema.Field field : fields) {
			row.put(field.getName(), valueOfAny(field.getType()));
		}
		return row;
	}


}
