package org.ananas.runner.steprunner.jdbc.mysql;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.ananas.runner.steprunner.jdbc.DDL;
import org.ananas.runner.steprunner.jdbc.JDBCDataType;
import org.apache.beam.sdk.extensions.sql.impl.utils.CalciteUtils;
import org.apache.beam.sdk.schemas.Schema.FieldType;

public enum MySQLDataTypes implements JDBCDataType, DDL {
  BIGINT("bigint", FieldType.INT64, true),
  BIGSERIAL("bigserial", FieldType.INT64, false),
  LONG("long", FieldType.INT64, false),
  SERIAL("serial", FieldType.INT64, false),
  SERIAL8("serial8", FieldType.INT64, false),
  BIGIINT_SERIAL8("bigint unsigned", FieldType.INT64, false),

  INTEGER("integer", FieldType.INT32, true),
  INT("int", FieldType.INT32, false),
  INT8("int8", FieldType.INT32, false),
  INT2("int2", FieldType.INT32, false),
  INT4("int4", FieldType.INT32, false),
  MEDIUMINT("mediumint", FieldType.INT32, false),
  TINYINT("tinyint", FieldType.INT32, false),
  SMALLINT("smallint", FieldType.INT32, false),
  BIT("bit", FieldType.INT32, false),

  DECIMAL("decimal", FieldType.DECIMAL, true),
  NUMERIC("numeric", FieldType.DECIMAL, false),
  FIXED("fixed", FieldType.DECIMAL, false),
  DEC("dec", FieldType.DECIMAL, false),

  FLOAT("float", FieldType.FLOAT, true),
  REAL("real", FieldType.FLOAT, false),
  SMALLFLOAT("smallfloat", FieldType.FLOAT, false),

  DOUBLE("double", FieldType.DOUBLE, true),
  DOUBLE_PRECISION("double precision", FieldType.DOUBLE, false),

  BOOLEAN("boolean", FieldType.BOOLEAN, true),
  BOOL("bool", FieldType.BOOLEAN, false),

  TEXT("text", FieldType.STRING, true),
  BYTES("bytes", FieldType.STRING, false),
  BINARY("binary", FieldType.STRING, false),
  VARBINARY("varbinary", FieldType.STRING, false),
  BYTE("byte", FieldType.INT16, false),
  BLOB("blob", FieldType.STRING, false),
  TINYBLOB("tinyblob", FieldType.STRING, false),
  MEDIUMBLOB("mediumblob", FieldType.STRING, false),
  LONGBLOB("longblob", FieldType.STRING, false),
  MEDIUMTEXT("mediumtext", FieldType.STRING, false),
  LONGTEXT("longtext", FieldType.STRING, false),
  TINYTEXT("tinytext", FieldType.STRING, false),
  STRING("string", FieldType.STRING, false),
  BITVAR("bit varying", FieldType.STRING, false),
  CHAR("char", FieldType.STRING, false),
  BPCHAR("bpchar", FieldType.STRING, false),
  CHARVAR("character varying", FieldType.STRING, false),
  CHARACTER("character", FieldType.STRING, false),
  VARCHAR("varchar", FieldType.STRING, false),
  LVARVARCHAR("lvarchar", FieldType.STRING, false),
  NVARCHAR("nvarchar", FieldType.STRING, false),
  OBJECT("object", FieldType.STRING, false),
  JSON("json", FieldType.STRING, false),
  XML("xml", FieldType.STRING, false),
  YEAR("year", FieldType.STRING, false),

  DATE_metadata("date", CalciteUtils.DATE, true),
  TIME_metadata("time", CalciteUtils.TIME, true),
  TIMESTAMP_metadata("timestamp", CalciteUtils.TIMESTAMP, true),
  TIMESTAMP_WITHOUT_TS_metadata("timestamp without time zone", CalciteUtils.TIMESTAMP, false),
  TIMESTAMP_WITH_TIME_ZONE_metadata("timestamp with time zone", CalciteUtils.TIMESTAMP, false),
  TIMESTAMPZ_metadata("timestamptz", CalciteUtils.TIMESTAMP, false),
  DATETIME("DATETIME", CalciteUtils.DATE, false),
  DATE("date", CalciteUtils.DATE, false);

  private static final Map<String, FieldType> dataTypes;

  static {
    dataTypes = new HashMap<>();
    for (JDBCDataType t : MySQLDataTypes.values()) {
      dataTypes.put(t.getDatatypeLiteral().toLowerCase(), t.getFieldType());
    }
  }

  private String datatypeLiteral;
  private FieldType fieldType;
  private boolean isDefault;

  MySQLDataTypes(String datatypeLiteral, FieldType fieldType, boolean isDefault) {
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
    for (JDBCDataType t : MySQLDataTypes.values()) {
      if ((Objects.deepEquals(t.getFieldType().withNullable(false), type)
              || Objects.deepEquals(t.getFieldType().withNullable(true), type))
          && t.isDefault()) {
        return t;
      }
    }
    return null;
  }

  public FieldType getDefaultDataTypeLiteral(String datatypeLiteral) {
    return dataTypes.get(datatypeLiteral);
  }

  public String rewrite(String url) {
    return url
        + "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
  }
}
