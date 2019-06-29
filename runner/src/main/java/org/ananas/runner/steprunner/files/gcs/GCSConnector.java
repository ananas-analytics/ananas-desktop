package org.ananas.runner.steprunner.files.gcs;

import org.ananas.runner.kernel.ConnectorStepRunner;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.kernel.model.StepType;
import org.ananas.runner.kernel.paginate.AutoDetectedSchemaPaginator;
import org.ananas.runner.kernel.paginate.PaginatorFactory;
import org.ananas.runner.steprunner.files.csv.BeamTextCSVCustomTable;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.commons.csv.CSVFormat;

public class GCSConnector extends ConnectorStepRunner {
  public GCSConnector(Pipeline pipeline, Step step, boolean doSampling, boolean isTest) {
    super(pipeline, step, doSampling, isTest);
  }

  public void build() {
    String format = (String) step.config.getOrDefault("format", "");
    switch (format) {
      case "csv":
        buildCsvConnector();
        break;
      default:
        break;
    }
  }

  private void buildCsvConnector() {
    GcsCsvConfig config = new GcsCsvConfig(StepType.Connector, step.config);

    CSVFormat format =
        CSVFormat.DEFAULT
            .withDelimiter(config.delimiter)
            .withRecordSeparator(config.recordSeparator);

    Schema schema = step.getBeamSchema();
    if (schema == null || step.forceAutoDetectSchema()) {
      // find the paginator bind to it
      AutoDetectedSchemaPaginator csvPaginator =
          PaginatorFactory.of(stepId, step.metadataId, step.type, step.config, schema)
              .buildPaginator();
      schema = csvPaginator.getSchema();
    }

    this.stepId = stepId;

    this.output =
        new BeamTextCSVCustomTable(
                this.errors, schema, config.url, format, config.hasHeader, doSampling, isTest)
            .buildIOReader(pipeline);
    this.output.setRowSchema(schema);
  }
}
