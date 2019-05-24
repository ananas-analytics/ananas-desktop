package org.ananas.runner.model.schema;

import java.io.Serializable;
import java.sql.*;
import java.util.Calendar;
import java.util.TimeZone;
import org.ananas.runner.model.datatype.TypeInferer;
import org.ananas.runner.model.steps.db.jdbc.JDBCDriver;
import org.ananas.runner.model.steps.db.jdbc.JDBCStatement;
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
    LOG.debug("Schema autodetect SQL {}", sql);
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
    Schema.Builder builder = Schema.builder();
    ResultSetMetaData metadata = rs.getMetaData();

    for (int i = 1; i <= metadata.getColumnCount(); i++) {
      String name = metadata.getColumnName(i);
      String typeName =
          metadata.getColumnTypeName(i) != null
              ? metadata.getColumnTypeName(i).toLowerCase()
              : "text";
      builder.addNullableField(
          name,
          driver.getDefaultDataTypeLiteral(typeName) == null
              ? Schema.FieldType.STRING
              : driver.getDefaultDataTypeLiteral(typeName));
    }
    return builder.build();
  }

  public static Object autoCast(ResultSet resultSet, int idx, Schema schema) {
    Schema.FieldType type = schema.getField(idx).getType();
    try {
      if (type.getTypeName().equals(Schema.FieldType.DATETIME.getTypeName())) {
        String metadata = String.valueOf(type.getMetadata());
        if (metadata.equals("TIME")) {
          Time ts = resultSet.getTime(idx + 1, UTC);
          return new DateTime(ts);
        }
        if (metadata.equals("DATE") || metadata.equals("TS")) {
          Timestamp ts = resultSet.getTimestamp(idx + 1, UTC);
          return new DateTime(ts);
        }
        Timestamp ts = resultSet.getTimestamp(idx + 1);
        return new DateTime(ts).toInstant();
      }
      Class clazz = TypeInferer.getClass(type);
      return resultSet.getObject(idx + 1, clazz);
    } catch (Exception e) {
      LOG.warn(
          "FETCH AUTOCAST WARNING : " + "idx= " + idx + " type: " + type + "  \n" + e.getMessage());
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
