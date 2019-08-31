package org.ananas.runner.steprunner.gcs;

import java.io.IOException;
import org.ananas.runner.core.ConnectorStepRunner;
import org.ananas.runner.core.common.JsonStringBasedFlattenerReader;
import org.ananas.runner.core.common.Sampler;
import org.ananas.runner.core.model.Step;
import org.ananas.runner.core.model.StepType;
import org.ananas.runner.core.paginate.AutoDetectedSchemaPaginator;
import org.ananas.runner.core.paginate.PaginatorFactory;
import org.ananas.runner.core.schema.SchemaBasedRowConverter;
import org.ananas.runner.paginator.files.GCSPaginator;
import org.ananas.runner.steprunner.files.csv.BeamTextCSVCustomTable;
import org.ananas.runner.steprunner.files.txt.TruncatedTextIO;
import org.ananas.runner.steprunner.files.utils.StepFileConfigToUrl;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.PCollection;
import org.apache.commons.csv.CSVFormat;

public class GCSConnector extends ConnectorStepRunner {

  private static final long serialVersionUID = -393275056610716725L;

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
    AutoDetectedSchemaPaginator csvPaginator =
        PaginatorFactory.of(stepId, step.metadataId, step.type, step.config, schema)
            .buildPaginator();
    if (schema == null || step.forceAutoDetectSchema()) {
      // find the paginator bind to it
      schema = csvPaginator.getSchema();
    }

    String url = config.url;
    // TODO: better not force paginator type here, move getSampleFileUrl method to some helper
    // class?
    if (isTest && csvPaginator instanceof GCSPaginator) {
      try {
        url = ((GCSPaginator) csvPaginator).getSampleFileUrl(step.config);
      } catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException("Failed to get sample file");
      }
    }

    this.output =
        new BeamTextCSVCustomTable(
                this.errors, schema, url, format, config.hasHeader, doSampling, isTest)
            .buildIOReader(pipeline);
    this.output.setRowSchema(schema);
  }

  private void buildJsonConnector() {
    String url = StepFileConfigToUrl.gcsSourceUrl(this.step.config);

    Schema schema = step.getBeamSchema();
    AutoDetectedSchemaPaginator paginator =
        PaginatorFactory.of(stepId, step.metadataId, step.type, step.config, schema)
            .buildPaginator();
    if (schema == null || step.forceAutoDetectSchema()) {
      schema = paginator.getSchema();
    }

    // TODO: better not force paginator type here, move getSampleFileUrl method to some helper
    // class?
    if (isTest && paginator instanceof GCSPaginator) {
      try {
        url = ((GCSPaginator) paginator).getSampleFileUrl(step.config);
      } catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException("Failed to get sample file");
      }
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
