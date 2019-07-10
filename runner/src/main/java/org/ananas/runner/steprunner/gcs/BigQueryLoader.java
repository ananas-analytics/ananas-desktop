package org.ananas.runner.steprunner.gcs;

import static org.apache.beam.sdk.values.Row.toRow;

import com.google.api.services.bigquery.model.TableRow;
import org.ananas.runner.kernel.LoaderStepRunner;
import org.ananas.runner.kernel.StepRunner;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.misc.StepConfigHelper;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.CreateDisposition;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.WriteDisposition;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryUtils;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.schemas.Schema.Builder;
import org.apache.beam.sdk.schemas.SchemaCoder;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BigQueryLoader extends LoaderStepRunner {

  private static final Logger LOG = LoggerFactory.getLogger(BigQueryLoader.class);

  public static final String PROJECT = "project";
  public static final String DATASET = "dataset";
  public static final String TABLE_NAME = "tablename";
  public static final String OVERWRITE = "overwrite";

  protected BigQueryLoader(Step step, StepRunner previous, boolean isTest) {
    super(step, previous, isTest);
  }

  public void build() {
    String projectId = (String) step.config.getOrDefault(PROJECT, "");
    String dataset = (String) step.config.getOrDefault(DATASET, "");
    String tablename = (String) step.config.getOrDefault(TABLE_NAME, "");
    Boolean overwrite = StepConfigHelper.getConfig(step.config, OVERWRITE, Boolean.FALSE);

    Schema schema = ((SchemaCoder) previous.getOutput().getCoder()).getSchema();

    schema = renameSchemaFieldNameIfNecessary(schema);
    this.output = previous.getOutput();
    if (isTest) {
      // TODO: check configuration
    }

    Schema finalSchema = schema;
    previous
        .getOutput()
        .apply(
            ParDo.of(
                new DoFn<Row, TableRow>() {
                  @ProcessElement
                  public void processElement(
                      @Element Row row, OutputReceiver<TableRow> outputReceiver) {
                    Row renamedRow = row.getValues().stream().collect(toRow(finalSchema));
                    TableRow tableRow = BigQueryUtils.toTableRow(renamedRow);
                    outputReceiver.output(tableRow);
                  }
                }))
        .apply(
            BigQueryIO.writeTableRows()
                .withSchema(BigQueryUtils.toTableSchema(finalSchema))
                .withCreateDisposition(CreateDisposition.CREATE_IF_NEEDED)
                .withWriteDisposition(
                    overwrite ? WriteDisposition.WRITE_TRUNCATE : WriteDisposition.WRITE_APPEND)
                .to(projectId + ":" + dataset + "." + tablename));
  }

  private Schema renameSchemaFieldNameIfNecessary(Schema schema) {
    // TODO: scan whole string and replace unaccepted char to _

    Schema.Builder builder = new Builder();

    schema
        .getFields()
        .forEach(
            field -> {
              String name = renameFieldNameIfNecessary(field.getName());
              builder.addField(name, field.getType());
            });

    return builder.build();
  }

  private static String renameFieldNameIfNecessary(String oldName) {
    String name = oldName;
    if (name.contains(".")) {
      name = name.replace('.', '_');
    }
    if (name.contains("-")) {
      name = name.replace('-', '_');
    }
    return name;
  }
}
