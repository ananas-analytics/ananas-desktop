package org.ananas.runner.steprunner.subprocess.utils;

import java.util.List;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.reflect.ReflectData;
import org.apache.beam.vendor.guava.v20_0.com.google.common.collect.Lists;

/**
 * Port {@link org.apache.beam.sdk.schemas.utils.AvroUtils
 *
 * <p><p><p>}
 */
public class AvroUtils {

  public static Schema toAvroSchema(org.apache.beam.sdk.schemas.Schema beamSchema) {
    List<Schema.Field> fields = Lists.newArrayList();
    for (org.apache.beam.sdk.schemas.Schema.Field field : beamSchema.getFields()) {
      org.apache.avro.Schema fieldSchema = getFieldSchema(field.getType());
      org.apache.avro.Schema.Field recordField =
          new org.apache.avro.Schema.Field(
              field.getName(), fieldSchema, field.getDescription(), (Object) null);
      fields.add(recordField);
    }
    Schema avroSchema = org.apache.avro.Schema.createRecord("Object", null, "object.avro", false);
    avroSchema.setFields(fields);
    return avroSchema;
  }

  private static org.apache.avro.Schema getFieldSchema(
      org.apache.beam.sdk.schemas.Schema.FieldType fieldType) {
    org.apache.avro.Schema baseType;
    switch (fieldType.getTypeName()) {
      case BYTE:
      case INT16:
      case INT32:
        baseType = org.apache.avro.Schema.create(org.apache.avro.Schema.Type.INT);
        break;

      case INT64:
        baseType = org.apache.avro.Schema.create(org.apache.avro.Schema.Type.LONG);
        break;

      case DECIMAL:
        baseType =
            LogicalTypes.decimal(Integer.MAX_VALUE)
                .addToSchema(org.apache.avro.Schema.create(org.apache.avro.Schema.Type.BYTES));
        break;

      case FLOAT:
        baseType = org.apache.avro.Schema.create(org.apache.avro.Schema.Type.FLOAT);
        break;

      case DOUBLE:
        baseType = org.apache.avro.Schema.create(org.apache.avro.Schema.Type.DOUBLE);
        break;

      case STRING:
        baseType = org.apache.avro.Schema.create(org.apache.avro.Schema.Type.STRING);
        break;

      case DATETIME:
        // TODO: There is a desire to move Beam schema DATETIME to a micros representation. When
        // this is done, this logical type needs to be changed.
        baseType =
            LogicalTypes.timestampMillis()
                .addToSchema(org.apache.avro.Schema.create(org.apache.avro.Schema.Type.LONG));
        break;

      case BOOLEAN:
        baseType = org.apache.avro.Schema.create(org.apache.avro.Schema.Type.BOOLEAN);
        break;

      case BYTES:
        baseType = org.apache.avro.Schema.create(org.apache.avro.Schema.Type.BYTES);
        break;

      case LOGICAL_TYPE:
        org.apache.beam.sdk.schemas.utils.AvroUtils.FixedBytesField fixedBytesField =
            org.apache.beam.sdk.schemas.utils.AvroUtils.FixedBytesField.fromBeamFieldType(
                fieldType);
        if (fixedBytesField != null) {
          baseType = fixedBytesField.toAvroType();
        } else {
          throw new RuntimeException(
              "Unhandled logical type " + fieldType.getLogicalType().getIdentifier());
        }
        break;

      case ARRAY:
        baseType =
            org.apache.avro.Schema.createArray(
                getFieldSchema(fieldType.getCollectionElementType()));
        break;

      case MAP:
        if (fieldType.getMapKeyType().getTypeName().isStringType()) {
          // Avro only supports string keys in maps.
          baseType = org.apache.avro.Schema.createMap(getFieldSchema(fieldType.getMapValueType()));
        } else {
          throw new IllegalArgumentException("Avro only supports maps with string keys");
        }
        break;

      case ROW:
        baseType = toAvroSchema(fieldType.getRowSchema());
        break;

      default:
        throw new IllegalArgumentException("Unexpected type " + fieldType);
    }
    return fieldType.getNullable() ? ReflectData.makeNullable(baseType) : baseType;
  }
}
