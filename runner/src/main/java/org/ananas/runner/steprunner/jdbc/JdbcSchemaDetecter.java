package org.ananas.runner.steprunner.jdbc;

import java.io.Serializable;
import java.sql.*;
import java.util.Calendar;
import java.util.TimeZone;
import org.ananas.runner.kernel.schema.TypeInferer;
import org.apache.beam.sdk.schemas.Schema;
import org.joda.time.DateTime;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.RecordMapperProvider;
import org.jooq.RecordType;
import org.jooq.impl.DefaultRecordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcSchemaDetecter implements Serializable {

  private static final Logger LOG = LoggerFactory.getLogger(JdbcSchemaDetecter.class);
  private static final long serialVersionUID = -4553435614907307061L;

  private static Calendar UTC = Calendar.getInstance();

  static {
    UTC.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  public static Schema autodetect(
      JDBCDriver driver, String url, String username, String password, String sql) {
    // see https://github.com/pgjdbc/pgjdbc
    return JDBCStatement.Execute(
        driver,
        url,
        username,
        password,
        (conn, statement) -> {
          // statement.setMaxRows(100);
          ResultSet rs = statement.executeQuery(sql);
          // rs.setFetchSize(100);
          Schema schem = extractSchema(driver, rs);
          rs.close();
          return schem;
        });
  }

  public static Schema extractSchema(JDBCDriver driver, ResultSet rs) throws SQLException {
    LOG.debug("extracting schema");
    Schema.Builder builder = Schema.builder();
    ResultSetMetaData metadata = rs.getMetaData();

    for (int i = 1; i <= metadata.getColumnCount(); i++) {
      String name = metadata.getColumnName(i);
      String typeName =
          metadata.getColumnTypeName(i) != null
              ? metadata.getColumnTypeName(i).toLowerCase()
              : "text";
      LOG.debug(
          "extracting schema column '{}' with type name '{}' to Beam Type '{}'",
          name,
          typeName,
          driver.getDefaultDataTypeLiteral(typeName));
      builder.addNullableField(
          name,
          driver.getDefaultDataTypeLiteral(typeName) == null
              ? Schema.FieldType.STRING
              : driver.getDefaultDataTypeLiteral(typeName));
    }
    return builder.build();
  }

  public static Object autoCastDate(ResultSet resultSet, int idx, Schema.FieldType type)
      throws SQLException {
    String metadata = type.getMetadataString("subtype");
    if (metadata.equals("TIME")) {
      return new DateTime(resultSet.getTime(idx + 1));
    }
    if (metadata.equals("DATE")) {
      Date ts = resultSet.getDate(idx + 1, UTC);
      return new DateTime(ts);
    }
    if (metadata.equals("TS")) {
      Timestamp ts = resultSet.getTimestamp(idx + 1);
      return new DateTime(ts);
    }
    Timestamp ts = resultSet.getTimestamp(idx + 1);
    return new DateTime(ts).toInstant();
  }

  public static Object autoCast(ResultSet resultSet, int idx, Schema schema) {
    Schema.FieldType type = schema.getField(idx).getType();
    LOG.debug("Field Type {}", type);
    try {
      switch (type.getTypeName()) {
        case LOGICAL_TYPE:
          return autoCastDate(resultSet, idx, type);
        case DATETIME:
          return autoCastDate(resultSet, idx, type);
        case ARRAY:
          return resultSet.getArray(idx + 1);
        case STRING:
          return resultSet.getString(idx + 1);
        case BYTES:
          return resultSet.getBytes(idx + 1);
        case BYTE:
          return resultSet.getByte(idx + 1);
        case INT16:
          return resultSet.getInt(idx + 1);
        case INT32:
          return resultSet.getInt(idx + 1);
        case INT64:
          return resultSet.getLong(idx + 1);
        case DOUBLE:
          return resultSet.getDouble(idx + 1);
        case BOOLEAN:
          return resultSet.getBoolean(idx + 1);
        case DECIMAL:
          return resultSet.getBigDecimal(idx + 1);
        case FLOAT:
          return resultSet.getFloat(idx + 1);
        default:
          Class clazz = TypeInferer.getClass(type);
          return resultSet.getObject(idx + 1, clazz);
      }
    } catch (Exception e) {
      LOG.warn("FETCH AUTOCAST WARNING : idx= {} type: {}  \n {}", idx, type, e.getMessage());
    }
    return null;
  }

  public static class AnanasRecordMapper implements RecordMapperProvider {

    public AnanasRecordMapper() {}

    @Override
    public <R extends Record, E> RecordMapper<R, E> provide(
        RecordType<R> recordType, Class<? extends E> type) {
      // Fall back to jOOQ's DefaultRecordMapper, which maps records onto
      // POJOs using reflection.
      return new DefaultRecordMapper(recordType, type);
    }
  }
}
