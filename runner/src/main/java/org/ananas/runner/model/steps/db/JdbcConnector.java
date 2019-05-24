package org.ananas.runner.model.steps.db;

import static org.apache.beam.sdk.values.Row.toRow;

import java.io.Serializable;
import java.util.stream.IntStream;
import org.ananas.runner.model.schema.JdbcSchemaDetecter;
import org.ananas.runner.model.steps.commons.*;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.jdbc.JdbcIO;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.schemas.SchemaCoder;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.Row;

public class JdbcConnector extends AbstractStepRunner implements StepRunner, Serializable {

  private static final String LIMIT_50 = "LIMIT 50";
  private static final long serialVersionUID = -6997925716114568402L;
  private static final String LIMIT_PATTERN = "(LIMIT\\s+\\d+)\\s*;?\\s*$";

  public JdbcConnector(
      Pipeline pipeline, String stepId, JdbcStepConfig config, boolean doSampling, boolean isTest) {
    super(StepType.Connector);
    // config.sql = "select * from table_test2";
    JdbcPaginator paginator = new JdbcPaginator(stepId, config);
    Schema schema = paginator.autodetect();
    String sql = paginator.outputQuery(isTest).toString();
    this.stepId = stepId;

    PCollection<Row> p =
        pipeline.apply(
            JdbcIO.<Row>read()
                .withDataSourceConfiguration(
                    JdbcIO.DataSourceConfiguration.create(
                            config.driver.driverClassName, config.driver.ddl.rewrite(config.url))
                        .withUsername(config.username)
                        .withPassword(config.password))
                .withQuery(sql)
                .withCoder(SchemaCoder.of(schema))
                .withRowMapper(rowMapper(schema, this.errors)));

    this.output = Sampler.sample(p, 1000, (doSampling || isTest));

    this.output.setRowSchema(schema);
  }

  public static JdbcIO.RowMapper<Row> rowMapper(Schema schema, ErrorHandler handler) {
    return r -> {
      try {
        return IntStream.range(0, schema.getFieldCount())
            .mapToObj(idx -> JdbcSchemaDetecter.autoCast(r, idx, schema))
            .collect(toRow(schema));
      } catch (Exception e) {
        handler.addError(e);
        return null;
      }
    };
  }
}
