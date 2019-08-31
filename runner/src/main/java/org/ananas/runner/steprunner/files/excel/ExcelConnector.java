package org.ananas.runner.steprunner.files.excel;

import org.ananas.runner.core.ConnectorStepRunner;
import org.ananas.runner.core.errors.AnanasException;
import org.ananas.runner.core.errors.ErrorHandler;
import org.ananas.runner.core.errors.ExceptionHandler;
import org.ananas.runner.core.model.Step;
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
      ExcelPaginator paginator = new ExcelPaginator(stepId, step.type, step.config, null);
      r = paginator.paginateRows(0, Integer.MAX_VALUE);
    } catch (Exception e) {
      throw new AnanasException(
          ExceptionHandler.ErrorCode.CONNECTION,
          "A technical error occurred when reading your excel file. Please verify your parameters");
    }

    Schema schema = step.getBeamSchema();
    if (schema == null || step.forceAutoDetectSchema()) {
      // find the paginator bind to it
      schema = r.getLeft();
    }

    Create.Values<org.apache.beam.sdk.values.Row> pCollections;
    if (!r.getRight().iterator().hasNext()) {
      pCollections = Create.empty(schema);
    } else {
      pCollections = Create.of(r.getRight());
    }
    this.output = PBegin.in(pipeline).apply(pCollections);
    this.output.setRowSchema(schema);
  }
}
