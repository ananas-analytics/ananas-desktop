package org.ananas.runner.steprunner.gcs;

import com.google.api.services.bigquery.model.TableRow;
import org.ananas.runner.kernel.ConnectorStepRunner;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.kernel.paginate.AutoDetectedSchemaPaginator;
import org.ananas.runner.kernel.paginate.PaginatorFactory;
import org.ananas.runner.paginator.files.BigQuerySchemaDetector;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryUtils;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BigQueryConnector extends ConnectorStepRunner {
  private static final Logger LOG = LoggerFactory.getLogger(BigQueryConnector.class);

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
    // TableSchema s = BigQuerySchemaDetector.beamSchemaToTableSchema(finalSchema);
    String query = config.getQuery();
    this.output =
        pipeline
            .apply(BigQueryIO.readTableRows().fromQuery(query).usingStandardSql())
            .apply(
                ParDo.of(
                    new DoFn<TableRow, Row>() {
                      private static final long serialVersionUID = 1617056466865611645L;

                      @ProcessElement
                      public void processElement(ProcessContext c) {
                        TableRow tableRow = c.element();
                        LOG.warn(tableRow.getF().toString());
                        Row row =
                            BigQueryUtils.toBeamRow(
                                finalSchema,
                                BigQuerySchemaDetector.beamSchemaToTableSchema(finalSchema),
                                tableRow);
                        c.output(row);
                      }
                    }));
    this.output.setRowSchema(schema);
  }
}
