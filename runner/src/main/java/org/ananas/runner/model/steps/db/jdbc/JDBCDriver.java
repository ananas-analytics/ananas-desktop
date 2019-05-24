package org.ananas.runner.model.steps.db.jdbc;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import org.ananas.runner.model.steps.db.jdbc.derby.DerbyDataTypes;
import org.ananas.runner.model.steps.db.jdbc.derby.DerbySQLDialect;
import org.ananas.runner.model.steps.db.jdbc.mysql.MySQLDataTypes;
import org.ananas.runner.model.steps.db.jdbc.mysql.MySQLDialect;
import org.ananas.runner.model.steps.db.jdbc.pgsql.PostgresqlDataTypes;
import org.ananas.runner.model.steps.db.jdbc.pgsql.PostgresqlDialect;
import org.apache.beam.sdk.schemas.Schema;
import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum JDBCDriver implements Serializable {
  MYSQL(
      "mysql",
      "com.mysql.jdbc.Driver",
      MySQLDataTypes.BIGINT,
      MySQLDialect.of(),
      org.jooq.SQLDialect.MYSQL,
      true),
  POSTGRESQL(
      "postgres",
      "org.postgresql.Driver",
      PostgresqlDataTypes.BIGINT,
      PostgresqlDialect.of(),
      org.jooq.SQLDialect.POSTGRES,
      true),
  DERBY(
      "derby",
      "org.apache.derby.jdbc.EmbeddedDriver",
      DerbyDataTypes.BIGINT,
      DerbySQLDialect.of(),
      org.jooq.SQLDialect.DERBY,
      false),
  NONE("", "", PostgresqlDataTypes.BIGINT, null, org.jooq.SQLDialect.DEFAULT, false);

  public String driverClassName;
  public String driverName;
  public DDL ddl;
  public SQLDialect dialect;
  public org.jooq.SQLDialect JOOQdialect;
  public boolean authRequired;

  private static final Logger LOG = LoggerFactory.getLogger(JDBCDriver.class);

  private static final Map<Schema.TypeName, Integer> TYPES_CLASS = new HashMap<>();

  static {
    TYPES_CLASS.put(Schema.FieldType.INT32.getTypeName(), Types.INTEGER);
    TYPES_CLASS.put(Schema.FieldType.INT64.getTypeName(), Types.BIGINT);
    TYPES_CLASS.put(Schema.FieldType.DOUBLE.getTypeName(), Types.DOUBLE);
    TYPES_CLASS.put(Schema.FieldType.DECIMAL.getTypeName(), Types.DECIMAL);
    TYPES_CLASS.put(Schema.FieldType.FLOAT.getTypeName(), Types.FLOAT);
    TYPES_CLASS.put(Schema.FieldType.BYTE.getTypeName(), Types.BINARY);
    TYPES_CLASS.put(Schema.FieldType.BOOLEAN.getTypeName(), Types.BOOLEAN);
    TYPES_CLASS.put(Schema.FieldType.STRING.getTypeName(), Types.VARCHAR);
    TYPES_CLASS.put(Schema.FieldType.DATETIME.getTypeName(), Types.TIMESTAMP);
  }

  JDBCDriver(
      String driverName,
      String driverClassName,
      DDL ddl,
      SQLDialect dialect,
      org.jooq.SQLDialect jooqdialect,
      boolean authRequired) {
    this.driverClassName = driverClassName;
    this.driverName = driverName;
    this.ddl = ddl;
    this.dialect = dialect;
    this.JOOQdialect = jooqdialect;
    this.authRequired = authRequired;
  }

  public JDBCDriver getDriverByDriverClass(String driverClassName) {
    for (JDBCDriver d : JDBCDriver.values()) {
      if (d.driverClassName.equalsIgnoreCase(driverClassName)) {
        return d;
      }
    }
    return NONE;
  }

  public JDBCDriver getDriverByName(String name) {
    for (JDBCDriver d : JDBCDriver.values()) {
      if (d.driverName.equalsIgnoreCase(name)) {
        return d;
      }
    }
    throw new RuntimeException(
        String.format("%s is not supported yet. Please choose another DB name.", name));
  }

  public boolean isAuthRequired() {
    return this.authRequired;
  }

  public SQLDialect SQLDialect() {
    return this.dialect;
  }

  public JDBCDataType getDefaultDataType(Schema.FieldType fieldType) {
    return this.ddl.getDefaultDataType(fieldType);
  }

  public Schema.FieldType getDefaultDataTypeLiteral(String literal) {
    return this.ddl.getDefaultDataTypeLiteral(literal);
  }

  public void setParameter(int idx, Schema.FieldType type, PreparedStatement query, Object o) {
    try {
      if (o == null) {
        int ty = TYPES_CLASS.get(type.getTypeName());
        query.setNull(idx, ty);
        return;
      }
      switch (type.getTypeName()) {
        case DATETIME:
          String metadata = String.valueOf(type.getMetadata());
          if (metadata.equals("TIME")) {
            query.setTime(idx, new Time(((Instant) o).getMillis()));
            return;
          }
          if (metadata.equals("DATE") || metadata.equals("TS")) {
            query.setTimestamp(idx, new Timestamp(((Instant) o).getMillis()));
            return;
          }
          query.setTimestamp(idx, new Timestamp(((Instant) o).getMillis()));
          return;
        case BOOLEAN:
          query.setBoolean(idx, (Boolean) o);
          return;
        case INT64:
          query.setLong(idx, (Long) o);
          return;
        case STRING:
          query.setString(idx, (String) o);
          return;
        case DECIMAL:
          if (this == DERBY) {
            query.setBigDecimal(
                idx, o == null ? null : ((BigDecimal) o).setScale(2, BigDecimal.ROUND_DOWN));
          } else {
            query.setBigDecimal(idx, (BigDecimal) o);
          }
          return;
        case DOUBLE:
          query.setDouble(idx, (Double) o);
          return;
        case FLOAT:
          query.setFloat(idx, (Float) o);
          return;
        case INT16:
          query.setInt(idx, (Integer) o);
          return;
        case INT32:
          query.setInt(idx, (int) o);
          return;
        case BYTE:
          query.setByte(idx, (Byte) o);
          return;
      }
    } catch (Exception e) {
      LOG.debug(
          "setParameter AUTOCAST WARNING : "
              + "idx= "
              + idx
              + " type: "
              + type.getTypeName()
              + "  \n"
              + e.getMessage());
    }
  }

  public org.jooq.SQLDialect getJOOQdialect() {
    return this.JOOQdialect;
  }
}
