package org.ananas.runner.model.steps.api;

import java.io.IOException;
import java.io.Serializable;
import org.ananas.runner.kernel.AbstractStepRunner;
import org.ananas.runner.kernel.StepRunner;
import org.ananas.runner.kernel.errors.AnanasException;
import org.ananas.runner.kernel.errors.ErrorHandler;
import org.ananas.runner.kernel.errors.ExceptionHandler;
import org.ananas.runner.kernel.model.StepType;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.Row;
import org.apache.commons.lang3.tuple.MutablePair;

public class APIConnector extends AbstractStepRunner implements StepRunner, Serializable {

  private static final long serialVersionUID = 3622276763366208866L;

  public APIConnector(String stepId, Pipeline pipeline, APIStepConfig config) {
    super(StepType.Connector);
    this.stepId = stepId;
    this.errors = new ErrorHandler();

    MutablePair<Schema, Iterable<Row>> r = null;
    try {
      r = APIPaginator.handle(config);
    } catch (IOException e) {
      throw new AnanasException(
          ExceptionHandler.ErrorCode.CONNECTION,
          "A technical error occurred when connecting to your API. Please verify your parameters");
    }
    Create.Values<org.apache.beam.sdk.values.Row> pCollections = Create.of(r.getRight());
    this.output = PBegin.in(pipeline).apply(pCollections);
    this.output.setRowSchema(r.getLeft());
  }
}
