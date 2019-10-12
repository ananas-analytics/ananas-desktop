package org.ananas.runner.steprunner.gcs;

import static org.apache.beam.sdk.values.Row.toRow;

import com.google.api.services.bigquery.model.TableRow;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.TableResult;
import java.util.ArrayList;
import java.util.List;
import org.ananas.runner.core.ConnectorStepRunner;
import org.ananas.runner.core.model.Step;
import org.ananas.runner.core.paginate.AutoDetectedSchemaPaginator;
import org.ananas.runner.core.paginate.PaginatorFactory;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.transforms.Create;
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

  @Override
  public void build() {
    BigQueryStepConfig config = new BigQueryStepConfig(step.config);

    Schema schema = step.getBeamSchema();
    if (schema == null || step.forceAutoDetectSchema()) {
      // find the paginator bind to it
      AutoDetectedSchemaPaginator paginator =
          PaginatorFactory.of(
                  stepId, step.metadataId, step.type, step.config, schema, extensionManager)
              .buildPaginator();
      schema = paginator.getSchema();
    }

    if (isTest) {
      String testQuery = config.getQuery();
      if (!testQuery.toUpperCase().contains("LIMIT")) {
        testQuery += " LIMIT 1000";
      }
      TableResult result = BigQueryHelper.query(config.projectId, testQuery);

      List<Row> rowResults = new ArrayList<>();
      for (FieldValueList row : result.iterateAll()) {
        rowResults.add(BigQueryHelper.bigQueryRowToBeamRow(row, schema));
      }

      this.output = pipeline.apply(Create.of(rowResults).withRowSchema(schema));
      return;
    }

    Schema finalSchema = schema;
    String query = config.getQuery();
    this.output =
        pipeline
            .apply(BigQueryIO.readTableRows().fromQuery(query).usingStandardSql())
            .apply(
                ParDo.of(
                    new DoFn<TableRow, Row>() {
                      private static final long serialVersionUID = 1617056466865611645L;

                      @ProcessElement
                      public void processElement(
                          @Element TableRow tableRow, OutputReceiver<Row> outputReceiver) {
                        Row row =
                            finalSchema.getFields().stream()
                                .map(
                                    field ->
                                        BigQueryHelper.autoCast(
                                            field, tableRow.get(field.getName())))
                                .collect(toRow(finalSchema));
                        outputReceiver.output(row);
                      }
                    }));
    this.output.setRowSchema(schema);
  }
}
