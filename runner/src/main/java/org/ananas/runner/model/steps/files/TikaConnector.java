package org.ananas.runner.model.steps.files;

import java.io.Serializable;
import org.ananas.runner.kernel.AbstractStepRunner;
import org.ananas.runner.kernel.StepRunner;
import org.ananas.runner.kernel.common.Sampler;
import org.ananas.runner.kernel.model.StepType;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.tika.ParseResult;
import org.apache.beam.sdk.io.tika.TikaIO;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.PCollection;

public class TikaConnector extends AbstractStepRunner implements StepRunner, Serializable {

  private static final long serialVersionUID = 3622276763366208866L;

  public TikaConnector(
      String stepId, String url, Pipeline pipeline, boolean doSampling, boolean isTest) {
    super(StepType.Connector);
    final Schema schema =
        Schema.builder()
            .addField("text", Schema.FieldType.STRING)
            .addField("metadata", Schema.FieldType.STRING)
            .build();
    this.stepId = stepId;

    PCollection<ParseResult> p = pipeline.apply(TikaIO.parse().filepattern(url));
    this.output =
        Sampler.sample(p, 1000, (doSampling || isTest))
            .apply(new ParseResultReader(schema, this.errors));
    this.output.setRowSchema(schema);
  }
}
