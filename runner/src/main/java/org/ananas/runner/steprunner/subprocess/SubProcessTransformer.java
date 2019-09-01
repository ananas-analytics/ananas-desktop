package org.ananas.runner.steprunner.subprocess;

import org.ananas.runner.kernel.StepRunner;
import org.ananas.runner.kernel.TransformerStepRunner;
import org.ananas.runner.kernel.model.Step;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.schemas.utils.AvroUtils;
import org.apache.beam.sdk.transforms.ParDo;

public class SubProcessTransformer extends TransformerStepRunner {

  private static final long serialVersionUID = -64482147086202330L;

  SubProcessConfiguration configuration;
  Schema stdoutSchema;
  org.apache.avro.Schema stdoutAvroSchema;
  Schema stdinSchema;
  org.apache.avro.Schema stdinAvroSchema;

  public SubProcessTransformer(Step step, StepRunner previous) {
    super(step, previous);
    configuration = new SubProcessConfiguration(step.config);
  }

  public void build() {
    this.stdinSchema = previous.getSchema();
    this.stdinAvroSchema = AvroUtils.toAvroSchema(this.stdinSchema);
    this.stdoutAvroSchema = new org.apache.avro.Schema.Parser().parse(configuration.avroSchema);
    this.stdoutSchema = AvroUtils.toBeamSchema(this.stdoutAvroSchema);
    this.output =
        previous
            .getOutput()
            .apply(
                "subprocess transform",
                ParDo.of(
                    new InputDoFn(
                        configuration,
                        this.stdinSchema,
                        this.stdinAvroSchema.toString(),
                        this.stdoutSchema,
                        this.stdoutAvroSchema.toString())));
    this.output.setRowSchema(this.stdoutSchema);
  }
}
