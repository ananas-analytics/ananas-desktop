package org.ananas.runner.kernel;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.kernel.model.StepType;
import org.apache.beam.sdk.extensions.sql.SqlTransform;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.PCollectionTuple;
import org.apache.beam.sdk.values.TupleTag;

public class ConcatStepRunner extends AbstractStepRunner {

  public transient Step step;
  public transient StepRunner leftStep;
  public transient StepRunner rightStep;

  public ConcatStepRunner(Step step, StepRunner leftStep, StepRunner rightStep) {
    super(StepType.Connector);

    this.stepId = step.id;

    this.step = step;
    this.leftStep = leftStep;
    this.rightStep = rightStep;

  }

  public void build() {
    Preconditions.checkNotNull(leftStep);
    Preconditions.checkNotNull(rightStep);

    // hack for flink issue with Schema coder equals
    leftStep.getSchema().setUUID(null);
    rightStep.getSchema().setUUID(null);

    if (!leftStep.getSchema().equals(rightStep.getSchema())) {
      throw new RuntimeException("Both steps should have same columns (name and type). ");
    }

    PCollectionTuple collectionTuple =
        PCollectionTuple.of(new TupleTag<>("TableLeft"), leftStep.getOutput())
            .and(new TupleTag<>("TableRight"), rightStep.getOutput());

    this.output =
        collectionTuple.apply(
            SqlTransform.query(
                "SELECT "
                    + SQLProjection("TableLeft", leftStep.getSchema())
                    + " FROM TableLeft"
                    + " UNION  "
                    + "SELECT "
                    + SQLProjection("TableRight", rightStep.getSchema())
                    + " FROM TableRight "));
  }

  private String SQLProjection(String tableName, Schema schema) {
    return Joiner.on(" , ")
        .join(
            schema.getFields().stream()
                .map(c -> tableName + "." + "`" + c.getName() + "`")
                .iterator());
  }
}
