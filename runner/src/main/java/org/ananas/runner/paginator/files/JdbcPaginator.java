package org.ananas.runner.paginator.files;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.ananas.runner.kernel.errors.ErrorHandler;
import org.ananas.runner.kernel.paginate.AutoDetectedSchemaPaginator;
import org.ananas.runner.model.steps.db.JdbcConnector;
import org.ananas.runner.steprunner.jdbc.JdbcStepConfig;
import org.ananas.runner.steprunner.jdbc.JDBCStatement;
import org.ananas.runner.steprunner.jdbc.JdbcSchemaDetecter;
import org.apache.beam.sdk.io.jdbc.JdbcIO;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;
import org.jooq.Query;
import org.jooq.SelectQuery;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcPaginator extends AutoDetectedSchemaPaginator {

  private static final Logger LOG = LoggerFactory.getLogger(JdbcPaginator.class);

  JdbcStepConfig jdbcConfig;

  public JdbcPaginator(String id, String type, Map<String, Object> config, Schema schema) {
    super(id, type, config, schema);
  }

  @Override
  public Schema autodetect() {
    SelectQuery q = outputQuery(true);
    return JdbcSchemaDetecter.autodetect(
        this.jdbcConfig.driver,
        this.jdbcConfig.url,
        this.jdbcConfig.username,
        this.jdbcConfig.password,
        q.toString());
  }

  @Override
  public void parseConfig(Map<String, Object> config) {
    this.jdbcConfig = new JdbcStepConfig(config);
  }

  @Override
  public Iterable<Row> iterateRows(Integer page, Integer pageSize) {
    SelectQuery outputQuery = outputQuery(false);
    outputQuery.addLimit(page * pageSize, pageSize);

    LOG.info("Paginate Query : {}", outputQuery.getSQL());

    // JOOQ way . Still some issues with generic type erasure
    /*Result<Record> records = JDBCStatement.Execute(this.config.driver, this.config.url, this.config.username,
    		this.config.password,
    		(conn, statement) -> {
    			try (ResultSet rs = statement.executeQuery(outputQuery.getSQL())) {
    				return DSL.using(conn).fetch(rs);
    			}
    		}, true
    );
    Iterator<Row> rows = records.stream().map(record ->
    		(Row)IntStream.range(0, this.schemas.getFieldCount()).mapToObj(
    				(idx) -> {
    			return (Object)record.field(this.schemas.getField(idx).getName()).getValue(record);
    		}
    		).collect(Row.toRow(this.schemas))
    );*/

    ErrorHandler errors = new ErrorHandler();
    JdbcIO.RowMapper<org.apache.beam.sdk.values.Row> rowMapper =
        JdbcConnector.rowMapper(this.schema, errors);
    return JDBCStatement.Execute(
        this.jdbcConfig.driver,
        this.jdbcConfig.url,
        this.jdbcConfig.username,
        this.jdbcConfig.password,
        (conn, statement) -> {
          List<Row> results = new ArrayList<>();

          try (ResultSet rs = statement.executeQuery(outputQuery.toString())) {
            while (rs.next()) {
              try {
                Row r = rowMapper.mapRow(rs);
                if (r != null) {
                  results.add(r);
                }
              } catch (Exception e) {
                errors.addError(e);
              }
            }
          }
          return results;
        });
  }

  public SelectQuery outputQuery(boolean isLimit) {
    Query inputQuery =
        DSL.using(this.jdbcConfig.sqlDialect.JOOQdialect).parser().parseQuery(this.jdbcConfig.sql);
    String outputSQL = DSL.using(this.jdbcConfig.driver.JOOQdialect).render(inputQuery);
    SelectQuery q =
        (SelectQuery) DSL.using(this.jdbcConfig.driver.JOOQdialect).parser().parseQuery(outputSQL);
    if (isLimit) {
      q.addLimit(DEFAULT_LIMIT);
    }
    return q;
  }
}
