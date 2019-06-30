package org.ananas.runner.steprunner.gcs;

import org.ananas.runner.kernel.ConnectorStepRunner;
import org.ananas.runner.kernel.common.JsonStringBasedFlattenerReader;
import org.ananas.runner.kernel.common.Sampler;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.kernel.model.StepType;
import org.ananas.runner.kernel.paginate.AutoDetectedSchemaPaginator;
import org.ananas.runner.kernel.paginate.PaginatorFactory;
import org.ananas.runner.kernel.schema.SchemaBasedRowConverter;
import org.ananas.runner.steprunner.files.csv.BeamTextCSVCustomTable;
import org.ananas.runner.steprunner.files.txt.TruncatedTextIO;
import org.ananas.runner.steprunner.files.utils.StepFileConfigToUrl;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.PCollection;
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
      case "json":
        buildJsonConnector();
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

    this.output =
        new BeamTextCSVCustomTable(
                this.errors, schema, config.url, format, config.hasHeader, doSampling, isTest)
            .buildIOReader(pipeline);
    this.output.setRowSchema(schema);
  }

  private void buildJsonConnector() {
    String url = StepFileConfigToUrl.gcsSourceUrl(this.step.config);

    Schema schema = step.getBeamSchema();
    if (schema == null || step.forceAutoDetectSchema()) {
      // find the paginator bind to it
      AutoDetectedSchemaPaginator paginator =
          PaginatorFactory.of(stepId, step.metadataId, step.type, step.config, schema)
              .buildPaginator();
      schema = paginator.getSchema();
    }
    PCollection<String> p =
        this.pipeline.apply(
            this.isTest ? TruncatedTextIO.read().from(url) : TextIO.read().from(url));
    this.output =
        Sampler.sample(p, DEFAULT_LIMIT, (this.doSampling || this.isTest))
            .apply(
                new JsonStringBasedFlattenerReader(
                    SchemaBasedRowConverter.of(schema), this.errors));
    this.output.setRowSchema(schema);
  }
}
