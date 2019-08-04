package org.ananas.runner.steprunner.files.excel;

import org.ananas.runner.kernel.ConnectorStepRunner;
import org.ananas.runner.kernel.errors.AnanasException;
import org.ananas.runner.kernel.errors.ErrorHandler;
import org.ananas.runner.kernel.errors.ExceptionHandler;
import org.ananas.runner.kernel.model.Step;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.Row;
import org.apache.commons.lang3.tuple.MutablePair;

public class ExcelConnector extends ConnectorStepRunner {

  private static final long serialVersionUID = 3622276763366208866L;

  public ExcelConnector(Pipeline pipeline, Step step, boolean doSampling, boolean isTest) {
    super(pipeline, step, doSampling, isTest);
  }

  public void build() {
    this.errors = new ErrorHandler();

    MutablePair<Schema, Iterable<Row>> r = null;
    try {
      ExcelPaginator paginator = new ExcelPaginator(stepId, null, step.config, null);
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

    Create.Values<org.apache.beam.sdk.values.Row> pCollections = Create.of(r.getRight());
    this.output = PBegin.in(pipeline).apply(pCollections);
    this.output.setRowSchema(schema);
  }

}
