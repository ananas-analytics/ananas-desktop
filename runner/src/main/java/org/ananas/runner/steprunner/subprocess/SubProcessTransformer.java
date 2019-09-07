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
            .apply(
                "subprocess transform",
                ParDo.of(
                    new ExternalProgramDoFn(
                        configuration, previous.getSchema(), this.stdoutSchema)));
    this.output.setRowSchema(this.stdoutSchema);
  }
}
