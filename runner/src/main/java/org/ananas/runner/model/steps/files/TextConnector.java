package org.ananas.runner.model.steps.files;

import java.io.Serializable;
import org.ananas.runner.model.steps.commons.AbstractStepRunner;
import org.ananas.runner.model.steps.commons.Sampler;
import org.ananas.runner.model.steps.commons.StepRunner;
import org.ananas.runner.model.steps.commons.StepType;
import org.ananas.runner.model.steps.files.txt.TextReader;
import org.ananas.runner.model.steps.files.txt.TruncatedTextIO;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.PCollection;

public class TextConnector extends AbstractStepRunner implements StepRunner, Serializable {

  private static final long serialVersionUID = 6276754060061226946L;

  public TextConnector(
      String stepId, String url, Pipeline pipeline, boolean doSampling, boolean isTest) {
    super(StepType.Connector);
    final Schema schema = Schema.builder().addField("text", Schema.FieldType.STRING).build();
    this.stepId = stepId;
    PCollection<String> p =
        pipeline.apply(isTest ? TruncatedTextIO.read().from(url) : TextIO.read().from(url));
    this.output =
        Sampler.sample(p, 1000, (doSampling || isTest)).apply(new TextReader(schema, this.errors));
    this.output.setRowSchema(schema);
  }
}
