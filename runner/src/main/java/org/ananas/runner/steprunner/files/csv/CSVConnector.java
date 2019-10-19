package org.ananas.runner.steprunner.files.csv;

import org.ananas.runner.core.ConnectorStepRunner;
import org.ananas.runner.core.model.Step;
import org.ananas.runner.core.model.StepType;
import org.ananas.runner.core.paginate.AutoDetectedSchemaPaginator;
import org.ananas.runner.core.paginate.PaginatorFactory;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.commons.csv.CSVFormat;

public class CSVConnector extends ConnectorStepRunner {

  private static final long serialVersionUID = -6637097842918012864L;

  public CSVConnector(Pipeline pipeline, Step step, boolean doSampling, boolean isTest) {
    super(pipeline, step, doSampling, isTest);
  }

  @Override
  public void build() {
    CSVStepConfig config = new CSVStepConfig(StepType.Connector, step.config);

    CSVFormat format =
        CSVFormat.DEFAULT
            .withDelimiter(config.delimiter)
            .withRecordSeparator(config.recordSeparator);

    Schema schema = step.getBeamSchema();
    if (schema == null || step.forceAutoDetectSchema()) {
      // find the paginator bind to it
      AutoDetectedSchemaPaginator csvPaginator =
          PaginatorFactory.of(
                  stepId, step.metadataId, step.type, step.config, schema, extensionManager)
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
