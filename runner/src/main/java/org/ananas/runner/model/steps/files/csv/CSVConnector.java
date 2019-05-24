package org.ananas.runner.model.steps.files.csv;

import java.io.Serializable;
import org.ananas.runner.model.steps.commons.AbstractStepRunner;
import org.ananas.runner.model.steps.commons.StepType;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.commons.csv.CSVFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSVConnector extends AbstractStepRunner implements Serializable {

  private static final Logger LOG = LoggerFactory.getLogger(CSVConnector.class);
  private static final long serialVersionUID = -4031447533274691635L;

  public static SerializableFunction<String, Boolean> matchNonHeader(
      Schema schema, CSVFormat csvFormat) {
    // TODO add more fields
    String str =
        String.format(
            "%s%s%s%s",
            csvFormat.getQuoteCharacter(),
            schema.getField(0).getName(),
            csvFormat.getQuoteCharacter(),
            csvFormat.getDelimiter());
    return input -> !input.startsWith(str);
  }

  public CSVConnector(
      String stepId, CSVStepConfig config, Pipeline pipeline, boolean doSampling, boolean isTest) {
    super(StepType.Connector);
    CSVFormat format =
        CSVFormat.DEFAULT
            .withDelimiter(config.delimiter)
            .withRecordSeparator(config.recordSeparator);
    CSVPaginator csvPaginator = new CSVPaginator(stepId, config);
    Schema inputschema = csvPaginator.getSchema();
    this.stepId = stepId;

    this.output =
        new BeamTextCSVCustomTable(
                this.errors, inputschema, config.url, format, config.hasHeader, doSampling, isTest)
            .buildIOReader(pipeline);
    this.output.setRowSchema(inputschema);
  }
}
