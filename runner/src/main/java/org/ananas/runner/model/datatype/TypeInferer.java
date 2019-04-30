package org.ananas.runner.model.datatype;

import com.github.wnameless.json.flattener.JsonifyArrayList;
import com.github.wnameless.json.flattener.JsonifyLinkedHashMap;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TypeInferer {

	static Map<Schema.FieldType, List<Schema.FieldType>> COVARIANT_TYPES = new ConcurrentHashMap<>();
	static Map<Schema.FieldType, Class> TYPES_CLASS = new ConcurrentHashMap<>();

	static {
		//Type Promotion in Expressions
		//Largest Type That Appears in Expression
		COVARIANT_TYPES.put(Schema.FieldType.INT32,
				Arrays.asList(Schema.FieldType.INT64, Schema.FieldType.DOUBLE, Schema.FieldType.FLOAT,
						Schema.FieldType.STRING));
		COVARIANT_TYPES.put(Schema.FieldType.INT64,
				Arrays.asList(Schema.FieldType.DOUBLE, Schema.FieldType.FLOAT, Schema.FieldType.STRING));
		COVARIANT_TYPES.put(Schema.FieldType.DOUBLE, Arrays.asList(Schema.FieldType.FLOAT, Schema.FieldType.STRING));
		COVARIANT_TYPES.put(Schema.FieldType.FLOAT, Arrays.asList(Schema.FieldType.STRING));
		COVARIANT_TYPES.put(Schema.FieldType.DECIMAL, Arrays.asList(Schema.FieldType.STRING));
		COVARIANT_TYPES.put(Schema.FieldType.BYTE, Arrays.asList(Schema.FieldType.STRING));
		COVARIANT_TYPES.put(Schema.FieldType.BOOLEAN, Arrays.asList(Schema.FieldType.STRING));
		COVARIANT_TYPES.put(Schema.FieldType.STRING, Arrays.asList(Schema.FieldType.STRING));
		COVARIANT_TYPES.put(Schema.FieldType.DATETIME, Arrays.asList(Schema.FieldType.STRING));

		TYPES_CLASS.put(Schema.FieldType.INT32, Integer.class);
		TYPES_CLASS.put(Schema.FieldType.INT64, Long.class);
		TYPES_CLASS.put(Schema.FieldType.DOUBLE, Double.class);
		TYPES_CLASS.put(Schema.FieldType.DECIMAL, BigDecimal.class);
		TYPES_CLASS.put(Schema.FieldType.FLOAT, Float.class);
		TYPES_CLASS.put(Schema.FieldType.BYTE, Byte[].class);
		TYPES_CLASS.put(Schema.FieldType.BOOLEAN, Boolean.class);
		TYPES_CLASS.put(Schema.FieldType.STRING, String.class);
		TYPES_CLASS.put(Schema.FieldType.DATETIME, DateTime.class);
	}

	/**
	 * Merge Type by applying the Type Promotion concept : Largest Type is returned
	 *
	 * @param one
	 * @param other
	 */
	public static Schema.FieldType mergeType(Schema.FieldType one, Schema.FieldType other) {
		if (Objects.equals(one, other)) {
			return one;
		}

		if (one.getTypeName().isPrimitiveType() && COVARIANT_TYPES.get(one).contains(other)) {
			return other;
		}

		if (one.getTypeName().isCompositeType() && other.getTypeName().isCompositeType()) {
			//merge
			if (Objects.equals(one.getRowSchema(), other.getRowSchema())) {
				return one;
			} else {
				Schema.Builder b = Schema.builder();
				for (Schema.Field otherField : other.getRowSchema().getFields()) {
					Schema.Field oneField = one.getRowSchema().getField(otherField.getName());
					if (oneField == null) {
						b.addNullableField(otherField.getName(), otherField.getType());
					} else {
						b.addNullableField(oneField.getName(), mergeType(oneField.getType(), otherField.getType()));
					}
				}
				return Schema.FieldType.row(b.build());
			}
		}

		if (one.getTypeName().isCollectionType() && other.getTypeName().isCollectionType()) {
			if (Objects.equals(one.getCollectionElementType(), other.getCollectionElementType())) {
				return one;
			} else {
				//when an array is empty , the default element type is string
				//return the type that is not string if any exists
				if (!one.getCollectionElementType().getTypeName().isStringType() &&
						!other.getCollectionElementType().getTypeName().isStringType()) {
					return Schema.FieldType.array(
							mergeType(one.getCollectionElementType(), other.getCollectionElementType()));
				} else if (!one.getCollectionElementType().getTypeName().isStringType()) {
					return one;
				} else {
					return other;
				}
			}
		}
		return Schema.FieldType.STRING;//TODO should throw an exception here
	}

	/**
	 * Attempts to interpret the Object <tt>value</tt> as a representation
	 * of any type. If the attempt is successful the type is returned.
	 *
	 * @param value
	 * @param parseString if true, infer type from string value by parsing it
	 * @return the field type
	 */
	public static Schema.FieldType inferType(Object value, boolean parseString) {
		if (value == null) {
			return Schema.FieldType.STRING;
		}
		if (value.getClass().isAssignableFrom(Map.class) ||
				value.getClass().isAssignableFrom(JsonifyLinkedHashMap.class)) {
			Map l = (Map) value;
			Schema.Builder b = Schema.builder();
			for (Object key : l.keySet()) {
				Schema.FieldType valueType = inferType(l.get(key), false);
				b.addNullableField((String) key, valueType);
			}
			return Schema.FieldType.row(b.build());
		}
		if (value.getClass().isAssignableFrom(List.class) || value.getClass().isAssignableFrom(ArrayList.class) ||
				value.getClass().isAssignableFrom(JsonifyArrayList.class)) {
			List l = (List) value;
			if (l.isEmpty()) {
				return Schema.FieldType.array(Schema.FieldType.STRING);
			}
			return Schema.FieldType.array(inferType(l.get(0), false));
		}
		if (value.getClass().isArray()) {
			Object[] l = (Object[]) value;
			if (l.length == 0) {
				return Schema.FieldType.array(Schema.FieldType.STRING);
			}
			return Schema.FieldType.array(inferType(l[0], false));
		}
		if (value.getClass().isAssignableFrom(Integer.class)) {
			return Schema.FieldType.INT32;
		}
		if (value.getClass().isAssignableFrom(Long.class)) {
			return Schema.FieldType.INT64;
		}
		if (value.getClass().isAssignableFrom(Double.class)) {
			return Schema.FieldType.DOUBLE;
		}
		if (value.getClass().isAssignableFrom(Float.class)) {
			return Schema.FieldType.FLOAT;
		}
		if (value.getClass().isAssignableFrom(BigDecimal.class)) {
			return Schema.FieldType.DECIMAL;
		}
		if (value.getClass().isAssignableFrom(Boolean.class)) {
			return Schema.FieldType.BOOLEAN;
		}
		if (value.getClass().isAssignableFrom(Byte.class)) {
			return Schema.FieldType.BYTE;
		}
		if (value.getClass().isAssignableFrom(Timestamp.class) || value.getClass().isAssignableFrom(Date.class) ||
				value.getClass().isAssignableFrom(Instant.class)) {
			return Schema.FieldType.DATETIME;
		}
		if (parseString) {
			return inferTypeFromString(value.toString());//we try to infer type from a string value
		}
		return Schema.FieldType.STRING;
	}

	// Attempts to interpret the string <tt>value</tt> as a representation
	// of any type. If the attempt is successful the type is returned.
	//
	public static Schema.FieldType inferTypeFromString(String value) {
		try {
			Integer.valueOf(value);
			return Schema.FieldType.INT32;
		} catch (Exception e) {
		}
		try {
			Long.valueOf(value);
			return Schema.FieldType.INT64;
		} catch (Exception e) {
		}
		try {
			Double.valueOf(value);
			return Schema.FieldType.DOUBLE;
		} catch (Exception e) {
		}
		try {
			Float.valueOf(value);
			return Schema.FieldType.FLOAT;
		} catch (Exception e) {
		}
		try {
			new BigDecimal(value);
			return Schema.FieldType.DECIMAL;
		} catch (Exception e) {
		}
		//if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
		//  return Schema.FieldType.BOOLEAN;
		//}
		try {
			Byte.valueOf(value);
			return Schema.FieldType.BYTE;
		} catch (Exception e) {
		}
		try {
			Timestamp.valueOf(value);
			return Schema.FieldType.DATETIME;
		} catch (Exception e) {
		}
		return Schema.FieldType.STRING;
	}

	/**
	 * Get class matching a given Schema Type
	 *
	 * @param type
	 * @return
	 */
	public static Class getClass(Schema.FieldType type) {
		if (type.getTypeName().isCompositeType()) {
			return Row.class;
		} else if (type.getTypeName().isCollectionType()) {
			return ArrayList.class;
		} else {
			return TYPES_CLASS.getOrDefault(type, String.class);
		}
	}

}
