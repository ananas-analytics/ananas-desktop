package org.ananas.runner.steprunner.jdbc.pgsql;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.ananas.runner.steprunner.jdbc.DDL;
import org.ananas.runner.steprunner.jdbc.JDBCDataType;
import org.apache.beam.sdk.extensions.sql.impl.utils.CalciteUtils;
import org.apache.beam.sdk.schemas.Schema.FieldType;

public enum PostgresqlDataTypes implements JDBCDataType, DDL {
  /**
   * MUST make sure that for each FieldType (includes metadata), ONLY "ONE" type is marked as
   * default true, group them by FieldType
   */

  // FieldType.INT64 mapping, note the first default true will be used to map fieldtype to literal
  BIGINT("bigint", FieldType.INT64, true),
  BIGSERIAL("bigserial", FieldType.INT64, false),
  LONG("long", FieldType.INT64, false),
  SERIAL("serial", FieldType.INT64, false),
  SERIAL4("serial4", FieldType.INT64, false),
  SERIAL8("serial8", FieldType.INT64, false),

  // FieldType.INT32
  INTEGER("integer", FieldType.INT32, true),
  INT("int", FieldType.INT32, false),
  INT2("int2", FieldType.INT32, false),
  INT4("int4", FieldType.INT32, false),
  INT8("int8", FieldType.INT32, false),
  SMALLINT("smallint", FieldType.INT32, false),
  SMALLSERIAL("smallserial", FieldType.INT32, false),
  SERIAL2("serial2", FieldType.INT32, false),

  // FieldType.STRING mapping
  CHARVAR("character varying", FieldType.STRING, true),
  OID("oid", FieldType.STRING, false),
  XID("xid", FieldType.STRING, false),
  BIT("bit", FieldType.STRING, false),
  BITVAR("bit varying", FieldType.STRING, false),
  UUID("uuid", FieldType.STRING, false),
  CHAR("char", FieldType.STRING, false),
  BPCHAR("bpchar", FieldType.STRING, false),
  CHARACTER("character", FieldType.STRING, false),
  JSON("json", FieldType.STRING, false),
  XML("xml", FieldType.STRING, false),
  VARCHAR("varchar", FieldType.STRING, false),
  STRING("string", FieldType.STRING, false),
  TEXT("text", FieldType.STRING, false),
  LVARVARCHAR("lvarchar", FieldType.STRING, false),
  NVARCHAR("nvarchar", FieldType.STRING, false),
  OBJECT("object", FieldType.STRING, false),
  MONEY("money", FieldType.STRING, false),

  // FieldType.BOOLEAN mapping
  BOOLEAN("boolean", FieldType.BOOLEAN, true),
  BOOL("bool", FieldType.BOOLEAN, false),

  // see: https://www.postgresql.org/docs/10/datatype.html

  // FieldType.BYTE
  BYTE("byte", FieldType.BYTE, true),

  // FieldType.BYTES
  BYTES("bytes", FieldType.BYTES, true),

  // FieldType.DECIMAL
  DECIMAL("decimal", FieldType.DECIMAL, true),
  NUMERIC("numeric", FieldType.DECIMAL, false),

  // FieldType.FLOAT
  REAL4("float4", FieldType.FLOAT, true),
  SMALLFLOAT("smallfloat", FieldType.FLOAT, false),
  REAL("real", FieldType.FLOAT, false),

  // FieldType.DOUBLE
  DOUBLE_PRECISION("double precision", FieldType.DOUBLE, true),
  FLOAT8("float8", FieldType.DOUBLE, false),

  // FieldType.DATETIME
  DATE_metadata("date", CalciteUtils.DATE, true),
  TIME_metadata("time", CalciteUtils.TIME, true),
  TIMESTAMP_metadata("timestamp", CalciteUtils.TIMESTAMP, true),
  TIMESTAMP_WITHOUT_TS_metadata("timestamp without time zone", CalciteUtils.TIMESTAMP, false),
  TIMESTAMP_WITH_TS_metadata("timestamp with time zone", CalciteUtils.TIMESTAMP, false),
  TIMEZ_metadata("timetz", CalciteUtils.TIME, false),
  INTERVAL_metadata("interval", FieldType.STRING, false),
  TIME_WITH_TIME_ZONE_metadata("time with time zone", CalciteUtils.TIME, false),
  TIME_WITHOUT_TIME_ZONE_metadata("time without time zone", CalciteUtils.TIME, false),
  TIMESTAMPZ_metadata("timestamptz", CalciteUtils.TIMESTAMP, false);

  private static final Map<String, FieldType> dataTypes;

  static {
    dataTypes = new HashMap<>();
    for (JDBCDataType t : PostgresqlDataTypes.values()) {
      dataTypes.put(t.getDatatypeLiteral().toLowerCase(), t.getFieldType());
    }
  }

  private String datatypeLiteral;
  private FieldType fieldType;
  private boolean isDefault;

  PostgresqlDataTypes(String datatypeLiteral, FieldType fieldType, boolean isDefault) {
    this.datatypeLiteral = datatypeLiteral;
    this.fieldType = fieldType;
    this.isDefault = isDefault;
  }

  public String getDatatypeLiteral() {
    return this.datatypeLiteral;
  }

  public FieldType getFieldType() {
    return this.fieldType;
  }

  public boolean isDefault() {
    return this.isDefault;
  }

  @Override
  public JDBCDataType getDefaultDataType(FieldType type) {
    for (JDBCDataType t : PostgresqlDataTypes.values()) {
      if ((Objects.deepEquals(t.getFieldType().withNullable(true), type)
              || Objects.deepEquals(t.getFieldType().withNullable(false), type))
          && t.isDefault()) {
        return t;
      }
    }
    return null;
  }

  public FieldType getDefaultDataTypeLiteral(String datatypeLiteral) {
    return dataTypes.get(datatypeLiteral.toLowerCase());
  }

  public String rewrite(String url) {
    return url;
  }
}
