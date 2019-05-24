package org.ananas.runner.model.schema;

import static org.apache.beam.sdk.values.Row.toRow;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.ananas.runner.model.errors.ExceptionHandler;
import org.ananas.runner.model.steps.commons.ErrorHandler;
import org.ananas.runner.model.steps.commons.RowConverter;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;
import org.joda.time.Instant;
import org.joda.time.base.AbstractInstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchemaBasedRowConverter implements RowConverter, Serializable {

  private static final Logger LOG = LoggerFactory.getLogger(SchemaBasedRowConverter.class);
  private static final long serialVersionUID = 1791772855843099453L;

  private ErrorHandler errors;
  protected Schema schema;

  private SchemaBasedRowConverter(Schema schema) {
    this.schema = schema;
    this.errors = new ErrorHandler();
  }

  public static SchemaBasedRowConverter of(Schema schema) {
    return new SchemaBasedRowConverter(schema);
  }

  @Override
  public Row convertMap(Map<String, Object> o) {
    return this.convertMap(o, this.schema);
  }

  private Row convertMap(Map<String, Object> o, Schema rowSchema) {
    return IntStream.range(0, rowSchema.getFieldCount())
        .mapToObj(
            idx ->
                convert(
                    o.get(rowSchema.getField(idx).getName()),
                    rowSchema.getField(idx).getType(),
                    rowSchema.getField(idx).getName()))
        .collect(toRow(rowSchema));
  }

  private Object convert(Object value, Schema.FieldType type, String fieldName) {
    if (value == null) {
      return null;
    }
    if (Schema.TypeName.ARRAY.equals(type.getTypeName())) {
      List<Object> arrayElements = convertArray(value, type.getCollectionElementType(), fieldName);
      return arrayElements;
    } else if (Schema.TypeName.MAP.equals(type.getTypeName())) {
      Map<Object, Object> mapElements =
          verifyMap(value, type.getMapKeyType().getTypeName(), type.getMapValueType(), fieldName);
      return mapElements;
    } else if (Schema.TypeName.ROW.equals(type.getTypeName())) {
      return convertRow(value, type.getRowSchema(), fieldName);
    } else {
      return convertPrimitiveType(value, type.getTypeName(), fieldName);
    }
  }

  private List<Object> convertArray(
      Object value, Schema.FieldType collectionElementType, String fieldName) {
    if (value == null) {
      return Collections.EMPTY_LIST;
    }

    if (!(value instanceof List)) {
      this.errors.addError(
          ExceptionHandler.ErrorCode.GENERAL,
          String.format(
              "For field name %s and array type expected List. Instead " + " value was %s.",
              fieldName, value));
      return Collections.EMPTY_LIST;
    }
    List<Object> valueList = (List<Object>) value;
    List<Object> verifiedList = Lists.newArrayListWithCapacity(valueList.size());
    for (Object listValue : valueList) {
      verifiedList.add(convert(listValue, collectionElementType, fieldName));
    }
    return verifiedList;
  }

  private Map<Object, Object> verifyMap(
      Object value, Schema.TypeName keyTypeName, Schema.FieldType valueType, String fieldName) {
    if (value == null) {
      return Collections.EMPTY_MAP;
    }
    if (!(value instanceof Map)) {
      this.errors.addError(
          ExceptionHandler.ErrorCode.GENERAL,
          String.format(
              "For field name %s and map type expected Map. Instead " + " value was %s.",
              fieldName, value));
      return Collections.EMPTY_MAP;
    }
    Map<Object, Object> valueMap = (Map<Object, Object>) value;
    Map<Object, Object> verifiedMap = Maps.newHashMapWithExpectedSize(valueMap.size());
    for (Map.Entry<Object, Object> kv : valueMap.entrySet()) {
      verifiedMap.put(
          convertPrimitiveType(kv.getKey(), keyTypeName, fieldName),
          convert(kv.getValue(), valueType, fieldName));
    }
    return verifiedMap;
  }

  private Row convertRow(Object value, Schema schema, String fieldName) {
    if (value == null) {
      return null;
    }
    if (!(value instanceof Row)) {
      if (value instanceof Map) {
        return convertMap((Map) value, schema);
      }
      this.errors.addError(
          ExceptionHandler.ErrorCode.GENERAL,
          String.format(
              "For field name %s expected Row type. " + "Instead value was %s.", fieldName, value));
      return null;
    }
    // No need to recursively validate the nested Row, since there's no way to build the
    // Row object without it validating.
    return (Row) value;
  }

  private Object convertPrimitiveType(Object value, Schema.TypeName type, String fieldName) {
    if (value == null) {
      return null;
    }
    if (type.isDateType()) {
      return convertDateTime(value, fieldName);
    } else {
      switch (type) {
        case BYTE:
          if (value instanceof Byte) {
            return value;
          }
          return Byte.MIN_VALUE;
        case INT16:
          if (value instanceof Short) {
            return value;
          }
          return 0;
        case INT32:
          if (value instanceof Integer) {
            return value;
          }
          return 0;
        case INT64:
          if (value instanceof Long) {
            return value;
          }
          return 0l;
        case DECIMAL:
          if (value instanceof BigDecimal) {
            return value;
          }
          return new BigDecimal(0);
        case FLOAT:
          if (value instanceof Float) {
            return value;
          }
          return new Float(0);
        case DOUBLE:
          if (value instanceof Double) {
            return value;
          }
          return new Double(0);
        case STRING:
          if (value == null) {
            return null;
          }
          if (value instanceof String) {
            return value;
          }
          return value.toString();
        case BOOLEAN:
          if (value instanceof Boolean) {
            return value;
          }
          return value != null;
        default:
          // Shouldn't actually get here, but we need this case to satisfy linters.
          this.errors.addError(
              ExceptionHandler.ErrorCode.GENERAL,
              String.format("Not a primitive type for field name %s: %s", fieldName, type));
          return null;
      }
    }
  }

  private Instant convertDateTime(Object value, String fieldName) {
    // We support the following classes for datetimes.
    if (value instanceof AbstractInstant) {
      return ((AbstractInstant) value).toInstant();
    } else {
      this.errors.addError(
          ExceptionHandler.ErrorCode.GENERAL,
          String.format(
              "For field name %s and DATETIME type got unexpected value %s ", fieldName, value));
      return null;
    }
  }
}
