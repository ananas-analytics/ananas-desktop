package org.ananas.runner.kernel.schema;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import org.apache.beam.sdk.coders.*;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;

public class NullableRowCoder extends CustomCoder<Row> {

  private static final long serialVersionUID = -4986949375227247189L;

  private NullableRowCoder(Schema schema) {
    this.schema = schema;
  }

  private static final ImmutableMap<Schema.TypeName, Coder> CODER_MAP =
      ImmutableMap.<Schema.TypeName, Coder>builder()
          .put(Schema.TypeName.BYTE, NullableCoder.of(ByteCoder.of()))
          .put(Schema.TypeName.INT16, NullableCoder.of(BigEndianShortCoder.of()))
          .put(Schema.TypeName.INT32, NullableCoder.of(BigEndianIntegerCoder.of()))
          .put(Schema.TypeName.INT64, NullableCoder.of(BigEndianLongCoder.of()))
          .put(Schema.TypeName.DECIMAL, NullableCoder.of(BigDecimalCoder.of()))
          .put(Schema.TypeName.FLOAT, NullableCoder.of(FloatCoder.of()))
          .put(Schema.TypeName.DOUBLE, NullableCoder.of(DoubleCoder.of()))
          .put(Schema.TypeName.STRING, NullableCoder.of(StringUtf8Coder.of()))
          .put(Schema.TypeName.DATETIME, NullableCoder.of(InstantCoder.of()))
          .put(Schema.TypeName.BOOLEAN, NullableCoder.of(BooleanCoder.of()))
          .build();

  private static final ImmutableMap<Schema.TypeName, Integer> ESTIMATED_FIELD_SIZES =
      ImmutableMap.<Schema.TypeName, Integer>builder()
          .put(Schema.TypeName.BYTE, Byte.BYTES)
          .put(Schema.TypeName.INT16, Short.BYTES)
          .put(Schema.TypeName.INT32, Integer.BYTES)
          .put(Schema.TypeName.INT64, Long.BYTES)
          .put(Schema.TypeName.FLOAT, Float.BYTES)
          .put(Schema.TypeName.DOUBLE, Double.BYTES)
          .put(Schema.TypeName.DECIMAL, 32)
          .put(Schema.TypeName.BOOLEAN, 1)
          .put(Schema.TypeName.DATETIME, Long.BYTES)
          .build();

  private static final BitSetCoder nullListCoder = BitSetCoder.of();

  private Schema schema;

  /** Returns the coder used for a given primitive type. */
  private static <T> Coder<T> coderForPrimitiveType(Schema.TypeName typeName) {
    return (Coder<T>) CODER_MAP.get(typeName);
  }

  /** Return the estimated serialized size of a give row object. */
  private static long estimatedSizeBytes(Row row) {
    Schema schema = row.getSchema();
    int fieldCount = schema.getFieldCount();
    int bitmapSize = (((fieldCount - 1) >> 6) + 1) * 8;

    int fieldsSize = 0;
    for (int i = 0; i < schema.getFieldCount(); ++i) {
      fieldsSize += (int) estimatedSizeBytes(schema.getField(i).getType(), row.getValue(i));
    }
    return (long) bitmapSize + fieldsSize;
  }

  private static long estimatedSizeBytes(Schema.FieldType typeDescriptor, Object value) {
    switch (typeDescriptor.getTypeName()) {
      case ROW:
        return estimatedSizeBytes((Row) value);
      case ARRAY:
        List list = (List) value;
        long listSizeBytes = 0;
        for (Object elem : list) {
          listSizeBytes += estimatedSizeBytes(typeDescriptor.getCollectionElementType(), elem);
        }
        return 4 + listSizeBytes;
      case MAP:
        Map<Object, Object> map = (Map<Object, Object>) value;
        long mapSizeBytes = 0;
        for (Map.Entry<Object, Object> elem : map.entrySet()) {
          mapSizeBytes +=
              typeDescriptor.getMapKeyType().getTypeName().equals(Schema.TypeName.STRING)
                  ? ((String) elem.getKey()).length()
                  : ESTIMATED_FIELD_SIZES.get(typeDescriptor.getMapKeyType().getTypeName());
          mapSizeBytes += estimatedSizeBytes(typeDescriptor.getMapValueType(), elem.getValue());
        }
        return 4 + mapSizeBytes;
      case STRING:
        // Not always accurate - String.getBytes().length() would be more accurate here, but slower.
        return ((String) value).length();
      default:
        return ESTIMATED_FIELD_SIZES.get(typeDescriptor.getTypeName());
    }
  }

  public static NullableRowCoder of(Schema schema) {
    return new NullableRowCoder(schema);
  }

  public Schema getSchema() {
    return this.schema;
  }

  private Coder getCoder(Schema.FieldType fieldType) {
    if (Schema.TypeName.ARRAY.equals(fieldType.getTypeName())) {
      return ListCoder.of(getCoder(fieldType.getCollectionElementType()));
    } else if (Schema.TypeName.MAP.equals(fieldType.getTypeName())) {
      return MapCoder.of(
          coderForPrimitiveType(fieldType.getMapKeyType().getTypeName()),
          getCoder(fieldType.getMapValueType()));
    } else if (Schema.TypeName.ROW.equals((fieldType.getTypeName()))) {
      return org.apache.beam.sdk.coders.RowCoder.of(fieldType.getRowSchema());
    } else {
      return coderForPrimitiveType(fieldType.getTypeName());
    }
  }

  @Override
  public void encode(Row value, OutputStream outStream) throws IOException {
    nullListCoder.encode(scanNullFields(value), outStream);

    for (int idx = 0; idx < value.getFieldCount(); ++idx) {
      Schema.Field field = this.schema.getField(idx);
      if (value.getValue(idx) == null) {
        continue;
      }
      Coder coder = getCoder(field.getType());
      coder.encode(value.getValue(idx), outStream);
    }
  }

  @Override
  public Row decode(InputStream inStream) throws IOException {
    BitSet nullFields = nullListCoder.decode(inStream);
    List<Object> fieldValues = new ArrayList<>(this.schema.getFieldCount());
    for (int idx = 0; idx < this.schema.getFieldCount(); ++idx) {
      if (nullFields.get(idx)) {
        fieldValues.add(null);
      } else {
        Coder coder = getCoder(this.schema.getField(idx).getType());
        Object value = coder.decode(inStream);
        fieldValues.add(value);
      }
    }
    return Row.withSchema(this.schema).addValues(fieldValues).build();
  }

  /** Scan {@link Row} to find fields with a NULL value. */
  private BitSet scanNullFields(Row row) {
    BitSet nullFields = new BitSet(row.getFieldCount());
    for (int idx = 0; idx < row.getFieldCount(); ++idx) {
      if (row.getValue(idx) == null) {
        nullFields.set(idx);
      }
    }
    return nullFields;
  }

  @Override
  public void verifyDeterministic()
      throws org.apache.beam.sdk.coders.Coder.NonDeterministicException {}
}
