package org.ananas.runner.steprunner.subprocess;

import java.util.List;
import org.ananas.runner.kernel.StepRunner;
import org.ananas.runner.kernel.TransformerStepRunner;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.steprunner.subprocess.utils.CallingSubProcessUtils;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubProcessTransformer extends TransformerStepRunner {

  private static final long serialVersionUID = -64482147086202330L;

  SubProcessConfiguration configuration;
  Schema outputSchema;

  public SubProcessTransformer(Step step, StepRunner previous) {
    super(step, previous);
    configuration = new SubProcessConfiguration(step.config);
    this.outputSchema = Schema.builder().addStringField("output").build();
  }

  public void build() {
    this.output =
        previous
            .getOutput()
            .apply(
                "subprocess transform",
                ParDo.of(new EchoInputDoFn(configuration, "Echo", this.outputSchema)));
    this.output.setRowSchema(this.outputSchema);
  }

  /** Simple DoFn that echos the element, used as an example of running a C++ library. */
  @SuppressWarnings("serial")
  public static class EchoInputDoFn extends DoFn<Row, Row> {

    static final Logger LOG = LoggerFactory.getLogger(EchoInputDoFn.class);

    private SubProcessConfiguration configuration;
    private Schema schema;

    public EchoInputDoFn(SubProcessConfiguration configuration, String binary, Schema schema) {
      // Pass in configuration information the name of the filename of the sub-process and the level
      // of concurrency
      this.configuration = configuration;
      this.schema = schema;
    }

    @Setup
    public void setUp() throws Exception {
      CallingSubProcessUtils.setUp(configuration, configuration.executableName);
    }

    @ProcessElement
    public void processElement(ProcessContext c) throws Exception {
      try {

        // The ProcessingKernel deals with the execution of the process
        SubProcessKernel kernel = new SubProcessKernel(configuration);

        // Run the command and work through the results
        List<String> results = kernel.exec(String.valueOf(c.element().getString(0)));
        for (String s : results) {
          c.output(Row.withSchema(schema).addValue(s).build());
        }
      } catch (Exception ex) {
        LOG.error("Error processing element ", ex);
        throw ex;
      }
    }
  }
}
