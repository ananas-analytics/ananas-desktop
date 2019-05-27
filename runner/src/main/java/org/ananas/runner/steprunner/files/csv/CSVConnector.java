package org.ananas.runner.steprunner.files.csv;

import org.ananas.runner.kernel.ConnectorStepRunner;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.kernel.model.StepType;
import org.ananas.runner.paginator.files.CSVPaginator;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.commons.csv.CSVFormat;

public class CSVConnector extends ConnectorStepRunner {

  private static final long serialVersionUID = -6637097842918012864L;

  public CSVConnector(Pipeline pipeline, Step step, boolean doSampling, boolean isTest) {
    super(pipeline, step, doSampling, isTest);
  }

  public void build() {
    CSVStepConfig config = new CSVStepConfig(StepType.Connector, step.config);

    CSVFormat format =
        CSVFormat.DEFAULT
            .withDelimiter(config.delimiter)
            .withRecordSeparator(config.recordSeparator);

    // TODO: get schema from step dataframe if exist or if no force auto detection is specified
    CSVPaginator csvPaginator = new CSVPaginator(stepId, step.config, null);
    Schema inputschema = csvPaginator.getSchema();

    this.stepId = stepId;

    this.output =
        new BeamTextCSVCustomTable(
                this.errors, inputschema, config.url, format, config.hasHeader, doSampling, isTest)
            .buildIOReader(pipeline);
    this.output.setRowSchema(inputschema);
  }
}
