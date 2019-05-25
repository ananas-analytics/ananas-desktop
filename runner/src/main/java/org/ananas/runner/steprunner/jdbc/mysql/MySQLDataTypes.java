package org.ananas.runner.steprunner.jdbc.mysql;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.ananas.runner.steprunner.jdbc.DDL;
import org.ananas.runner.steprunner.jdbc.JDBCDataType;
import org.apache.beam.sdk.schemas.Schema;

public enum MySQLDataTypes implements JDBCDataType, DDL {
  BIGINT("bigint", Schema.FieldType.INT64, true),
  BIGSERIAL("bigserial", Schema.FieldType.INT64, true),
  BIT("bit", Schema.FieldType.STRING, false),
  BINARY("binary", Schema.FieldType.BYTE, false),

  BITVAR("bit varying", Schema.FieldType.STRING, false),
  BOOLEAN("boolean", Schema.FieldType.BOOLEAN, true),
  BOOL("bool", Schema.FieldType.BOOLEAN, false),
  BYTE("byte", Schema.FieldType.BYTE, false),
  BYTES("bytes", Schema.FieldType.BYTE, true),
  CHAR("char", Schema.FieldType.STRING, false),
  BPCHAR("bpchar", Schema.FieldType.STRING, false),
  CHARVAR("character varying", Schema.FieldType.STRING, false),
  CHARACTER("character", Schema.FieldType.STRING, false),
  DATE_metadata("date", Schema.FieldType.DATETIME.withMetadata("DATE"), true),
  TIME_metadata("time", Schema.FieldType.DATETIME.withMetadata("TIME"), true),
  TIMESTAMP_metadata("timestamp", Schema.FieldType.DATETIME.withMetadata("TS"), true),
  TIMESTAMP_WITHOUT_TS_metadata(
      "timestamp without time zone", Schema.FieldType.DATETIME.withMetadata("TS"), true),
  TIMESTAMP_WITH_TIME_ZONE_metadata(
      "timestamp with time zone", Schema.FieldType.DATETIME.withMetadata("TS_WITH_LOCAL_TZ"), true),
  TIMESTAMPZ_metadata(
      "timestamptz", Schema.FieldType.DATETIME.withMetadata("TS_WITH_LOCAL_TZ"), false),
  DECIMAL("decimal", Schema.FieldType.DECIMAL, false),
  REAL("real", Schema.FieldType.FLOAT, false),
  NUMERIC("numeric", Schema.FieldType.DECIMAL, true),
  DOUBLE_PRECISION("double precision", Schema.FieldType.DOUBLE, true),
  INT("int", Schema.FieldType.INT32, true),
  INTEGER("integer", Schema.FieldType.INT32, true),
  INT8("int8", Schema.FieldType.INT64, false),
  INT2("int2", Schema.FieldType.INT32, false),
  INT4("int4", Schema.FieldType.INT32, false),
  JSON("json", Schema.FieldType.STRING, false),
  LONG("long", Schema.FieldType.INT64, true),
  LVARVARCHAR("lvarchar", Schema.FieldType.STRING, false),
  NVARCHAR("nvarchar", Schema.FieldType.STRING, false),
  OBJECT("object", Schema.FieldType.STRING, false),
  SERIAL("serial", Schema.FieldType.INT64, false),
  SERIAL8("serial8", Schema.FieldType.INT64, false),
  SMALLFLOAT("smallfloat", Schema.FieldType.FLOAT, false),
  SMALLINT("smallint", Schema.FieldType.INT32, false),
  STRING("string", Schema.FieldType.STRING, false),
  TEXT("text", Schema.FieldType.STRING, true),
  TIMESTAMP("timestamp", Schema.FieldType.DATETIME, true),
  TIME("time", Schema.FieldType.DATETIME, true),
  VARCHAR("varchar", Schema.FieldType.STRING, false),
  DATE("date", Schema.FieldType.DATETIME, true),
  XML("xml", Schema.FieldType.STRING, false);

  private static final Map<String, Schema.FieldType> dataTypes;

  static {
    dataTypes = new HashMap<>();
    for (JDBCDataType t : MySQLDataTypes.values()) {
      dataTypes.put(t.getDatatypeLiteral().toLowerCase(), t.getFieldType());
    }
  }

  private String datatypeLiteral;
  private Schema.FieldType fieldType;
  private boolean isDefault;

  MySQLDataTypes(String datatypeLiteral, Schema.FieldType fieldType, boolean isDefault) {
    this.datatypeLiteral = datatypeLiteral;
    this.fieldType = fieldType;
    this.isDefault = isDefault;
  }

  public String getDatatypeLiteral() {
    return this.datatypeLiteral;
  }

  public Schema.FieldType getFieldType() {
    return this.fieldType;
  }

  public boolean isDefault() {
    return this.isDefault;
  }

  @Override
  public JDBCDataType getDefaultDataType(Schema.FieldType type) {
    for (JDBCDataType t : MySQLDataTypes.values()) {
      if (Objects.deepEquals(t.getFieldType(), type) && t.isDefault()) {
        return t;
      }
    }
    return null;
  }

  public Schema.FieldType getDefaultDataTypeLiteral(String datatypeLiteral) {
    return dataTypes.get(datatypeLiteral);
  }

  public String rewrite(String url) {
    return url
        + "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
  }
}
