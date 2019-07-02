package org.ananas.runner.steprunner.gcs;

import org.ananas.runner.kernel.ConnectorStepRunner;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.kernel.paginate.AutoDetectedSchemaPaginator;
import org.ananas.runner.kernel.paginate.PaginatorFactory;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryUtils;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryUtils.ConversionOptions;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.schemas.SchemaCoder;

public class BigQueryConnector extends ConnectorStepRunner {

  public BigQueryConnector(Pipeline pipeline, Step step, boolean doSampling, boolean isTest) {
    super(pipeline, step, doSampling, isTest);
  }

  public void build() {
    BigQueryStepConfig config = new BigQueryStepConfig(step.config);

    Schema schema = step.getBeamSchema();
    if (schema == null || step.forceAutoDetectSchema()) {
      // find the paginator bind to it
      AutoDetectedSchemaPaginator paginator =
          PaginatorFactory.of(stepId, step.metadataId, step.type, step.config, schema)
              .buildPaginator();
      schema = paginator.getSchema();
    }

    Schema finalSchema = schema;
    this.output =
        pipeline.apply(
            BigQueryIO.read(
                    record ->
                        BigQueryUtils.toBeamRow(
                            record.getRecord(), finalSchema, ConversionOptions.builder().build()))
                .fromQuery(config.getQuery())
                .usingStandardSql()
                .withCoder(SchemaCoder.of(schema)));

    this.output.setRowSchema(schema);
    /*
    this.output =
      PBegin.in(pipeline)
        .apply(
          BigQueryIO.read(
            (SchemaAndRecord elem) -> elem.getRecord())
          .fromQuery(
            "SELECT max_temperature FROM `clouddataflow-readonly.samples.weather_stations`")
          .usingStandardSql()
          .withCoder());

    */

  }
}
