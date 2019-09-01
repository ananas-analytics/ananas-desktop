package org.ananas.runner.kernel;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.List;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.kernel.model.StepType;
import org.ananas.runner.steprunner.subprocess.SubProcessConfiguration;
import org.ananas.runner.steprunner.subprocess.SubProcessKernel;
import org.ananas.runner.steprunner.subprocess.utils.CallingSubProcessUtils;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.schemas.utils.AvroUtils;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransformerStepRunner extends AbstractStepRunner {

  protected transient Step step;
  protected transient StepRunner previous;

  protected TransformerStepRunner(Step step, StepRunner previous) {
    super(StepType.Transformer);

    // for AbstractStepRunner
    this.stepId = step.id;

    this.step = step;
    this.previous = previous;
  }

  /** Simple DoFn that calls a library. */
  @SuppressWarnings("serial")
  public static class InputDoFn extends DoFn<Row, Row> implements Serializable {

    static final Logger LOG = LoggerFactory.getLogger(InputDoFn.class);

    private final SubProcessConfiguration configuration;
    private final Schema outputBeamSchema;
    private final String outputAvroSchema;
    private final Schema inputBeamSchema;
    private final String inputAvroSchema;

    public InputDoFn(
        SubProcessConfiguration configuration,
        Schema inputBeamSchema,
        String inputAvroSchema,
        Schema outputBeamSchema,
        String outputAvroSchema) {
      // Pass in configuration information the name of the filename of the sub-process and the level
      // of concurrency
      this.configuration = configuration;
      this.outputBeamSchema = outputBeamSchema;
      this.outputAvroSchema = outputAvroSchema;
      this.inputAvroSchema = inputAvroSchema;
      this.inputBeamSchema = inputBeamSchema;
    }

    @Setup
    public void setUp() throws Exception {
      CallingSubProcessUtils.setUp(configuration, configuration.executableName);
    }

    @ProcessElement
    public void processElement(ProcessContext c) throws Exception {
      try {

        // The ProcessingKernel deals with the execution of the process
        SubProcessKernel kernel = new SubProcessKernel(configuration, this.outputAvroSchema);

        org.apache.avro.Schema inputAvroSchemaObject =
            new org.apache.avro.Schema.Parser().parse(this.inputAvroSchema);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DatumWriter<GenericRecord> datumWriter =
            new GenericDatumWriter<GenericRecord>(inputAvroSchemaObject);
        DataFileWriter<GenericRecord> dataFileWriter =
            new DataFileWriter<GenericRecord>(datumWriter);
        dataFileWriter.create(inputAvroSchemaObject, outputStream);
        dataFileWriter.append(AvroUtils.toGenericRecord(c.element(), inputAvroSchemaObject));
        dataFileWriter.close();
        // Run the command and work through the results
        List<GenericRecord> results = kernel.exec(outputStream.toByteArray());
        for (GenericRecord s : results) {
          c.output(AvroUtils.toBeamRowStrict(s, this.outputBeamSchema));
        }
      } catch (Exception ex) {
        LOG.error("Error processing element ", ex);
        throw ex;
      }
    }
  }
}
