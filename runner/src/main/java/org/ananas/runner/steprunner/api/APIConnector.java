package org.ananas.runner.steprunner.api;

import java.util.ArrayList;
import java.util.List;
import org.ananas.runner.core.ConnectorStepRunner;
import org.ananas.runner.core.errors.AnanasException;
import org.ananas.runner.core.errors.ErrorHandler;
import org.ananas.runner.core.errors.ExceptionHandler;
import org.ananas.runner.core.model.LoopStepConfigGenerator;
import org.ananas.runner.core.model.Step;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.Row;
import org.apache.commons.lang3.tuple.MutablePair;

public class APIConnector extends ConnectorStepRunner {

  private static final long serialVersionUID = 3622276763366208866L;

  private LoopStepConfigGenerator configGenerator;

  public APIConnector(Pipeline pipeline, Step step, boolean doSampling, boolean isTest) {
    super(pipeline, step, doSampling, isTest);
    this.configGenerator = new LoopStepConfigGenerator(step.config);
  }

  @Override
  public void build() {
    this.errors = new ErrorHandler();

    MutablePair<Schema, Iterable<Row>> r = null;
    APIPaginator paginator = new APIPaginator(stepId, null, step.config, null);
    try {
      r = paginator.paginateRows(0, Integer.MAX_VALUE);
    } catch (Exception e) {
      throw new AnanasException(
          ExceptionHandler.ErrorCode.CONNECTION,
          "A technical error occurred when connecting to your API. Please verify your parameters");
    }

    Schema schema = step.getBeamSchema();
    if (schema == null || step.forceAutoDetectSchema()) {
      // find the paginator bind to it
      schema = r.getLeft();
    }

    int page = 0;
    List<Row> output = new ArrayList<>();
    do {
      try {
        r = paginator.paginateRows(page, Integer.MAX_VALUE);
        r.getRight().forEach(row -> output.add(row));
      } catch (Exception e) {
        this.errors.addError(e);
      } finally {
        page++;
      }
    } while (this.configGenerator.prepareNext());

    Create.Values<org.apache.beam.sdk.values.Row> pCollections = Create.of(output);
    this.output = PBegin.in(pipeline).apply(pCollections);
    this.output.setRowSchema(schema);
  }
}
