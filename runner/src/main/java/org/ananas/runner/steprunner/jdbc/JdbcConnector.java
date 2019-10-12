package org.ananas.runner.steprunner.jdbc;

import static org.apache.beam.sdk.values.Row.toRow;

import java.util.stream.IntStream;
import org.ananas.runner.core.ConnectorStepRunner;
import org.ananas.runner.core.common.Sampler;
import org.ananas.runner.core.errors.ErrorHandler;
import org.ananas.runner.core.model.Step;
import org.ananas.runner.core.paginate.AutoDetectedSchemaPaginator;
import org.ananas.runner.core.paginate.PaginatorFactory;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.jdbc.JdbcIO;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.schemas.SchemaCoder;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.Row;
import org.jooq.Query;
import org.jooq.SelectQuery;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcConnector extends ConnectorStepRunner {

  private static final Logger LOG = LoggerFactory.getLogger(JdbcConnector.class);

  private JdbcStepConfig config;

  public JdbcConnector(Pipeline pipeline, Step step, boolean doSampling, boolean isTest) {
    super(pipeline, step, doSampling, isTest);
  }

  @Override
  public void build() {
    config = new JdbcStepConfig(this.step.config);

    Schema stepSchema = step.getBeamSchema();
    if (stepSchema == null || step.forceAutoDetectSchema()) {
      AutoDetectedSchemaPaginator paginator =
          PaginatorFactory.of(
                  stepId, step.metadataId, step.type, step.config, stepSchema, extensionManager)
              .buildPaginator();
      // find the paginator bind to it
      stepSchema = paginator.getSchema();
    }
    String sql = outputQuery(isTest).toString();

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
                .withCoder(SchemaCoder.of(stepSchema))
                .withRowMapper(rowMapper(stepSchema, this.errors)));

    this.output = Sampler.sample(p, 1000, (doSampling || isTest));

    this.output.setRowSchema(stepSchema);
  }

  public static JdbcIO.RowMapper<Row> rowMapper(Schema schema, ErrorHandler handler) {
    return r -> {
      try {
        return IntStream.range(0, schema.getFieldCount())
            .mapToObj(idx -> JdbcSchemaDetecter.autoCast(r, idx, schema))
            .collect(toRow(schema));
      } catch (Exception e) {
        LOG.warn("error {}", e.toString());
        handler.addError(e);
        return null;
      }
    };
  }

  public SelectQuery outputQuery(boolean isLimit) {
    Query inputQuery =
        DSL.using(this.config.sqlDialect.JOOQdialect).parser().parseQuery(this.config.sql);
    String outputSQL = DSL.using(this.config.driver.JOOQdialect).render(inputQuery);
    SelectQuery q =
        (SelectQuery) DSL.using(this.config.driver.JOOQdialect).parser().parseQuery(outputSQL);
    if (isLimit) {
      q.addLimit(DEFAULT_LIMIT);
    }
    return q;
  }
}
