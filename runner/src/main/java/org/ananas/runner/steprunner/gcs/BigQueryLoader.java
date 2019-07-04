package org.ananas.runner.steprunner.gcs;

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
import org.apache.beam.sdk.schemas.SchemaCoder;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.windowing.BoundedWindow;
import org.apache.beam.sdk.values.Row;

public class BigQueryLoader extends LoaderStepRunner {

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

    this.output = previous.getOutput();
    if (isTest) {
      // TODO: check configuration
    }

    previous
        .getOutput()
        .apply(
            ParDo.of(
                new DoFn<Row, TableRow>() {
                  @ProcessElement
                  public void processElement(ProcessContext c, BoundedWindow window) {
                    c.output(BigQueryUtils.toTableRow(c.element()));
                  }
                }))
        .apply(
            BigQueryIO.writeTableRows()
                .to(projectId + ":" + dataset + "." + tablename)
                .withSchema(BigQueryUtils.toTableSchema(schema))
                .withCreateDisposition(CreateDisposition.CREATE_IF_NEEDED)
                .withWriteDisposition(overwrite ? WriteDisposition.WRITE_TRUNCATE : WriteDisposition.WRITE_APPEND));
  }
}
