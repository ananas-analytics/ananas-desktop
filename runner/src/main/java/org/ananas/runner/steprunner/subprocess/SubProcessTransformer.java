package org.ananas.runner.steprunner.subprocess;

import java.util.Random;
import org.ananas.runner.kernel.StepRunner;
import org.ananas.runner.kernel.TransformerStepRunner;
import org.ananas.runner.kernel.model.Step;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.schemas.utils.AvroUtils;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.Row;

public class SubProcessTransformer extends TransformerStepRunner {

  private static final long serialVersionUID = -64482147086202330L;

  SubProcessConfiguration configuration;
  Schema stdoutSchema;

  public SubProcessTransformer(Step step, StepRunner previous) {
    super(step, previous);
    configuration = new SubProcessConfiguration(step.config);
  }

  public void build() {
    org.apache.avro.Schema stdoutAvroSchema =
        new org.apache.avro.Schema.Parser().parse(configuration.avroSchema);
    this.stdoutSchema = AvroUtils.toBeamSchema(stdoutAvroSchema);
    this.output =
        previous
            .getOutput()
            .apply(ParDo.of(new AssignRandomKeys(configuration.concurrency)))
            .apply(GroupIntoBatches.<Integer, Row>ofSize(configuration.batchSize))
            .apply(
                "subprocess transform",
                ParDo.of(
                    new ExternalProgramDoFn(
                        configuration, previous.getSchema(), this.stdoutSchema)));
    this.output.setRowSchema(this.stdoutSchema);
  }

  /** Assigns to clicks random integer between zero and shardsNumber */
  private static class AssignRandomKeys extends DoFn<Row, KV<Integer, Row>> {
    private int shardsNumber;
    private Random random;

    AssignRandomKeys(int shardsNumber) {
      super();
      this.shardsNumber = shardsNumber;
    }

    @Setup
    public void setup() {
      random = new Random();
    }

    @DoFn.ProcessElement
    public void processElement(ProcessContext c) {
      KV kv = KV.<Integer, Row>of(random.nextInt(shardsNumber), c.element());
      c.output(kv);
    }
  }
}
