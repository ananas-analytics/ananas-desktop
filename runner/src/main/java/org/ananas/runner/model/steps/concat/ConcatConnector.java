package org.ananas.runner.model.steps.concat;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import java.io.Serializable;
import org.ananas.runner.model.steps.commons.AbstractStepRunner;
import org.ananas.runner.model.steps.commons.StepRunner;
import org.ananas.runner.model.steps.commons.StepType;
import org.apache.beam.sdk.extensions.sql.SqlTransform;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.PCollectionTuple;
import org.apache.beam.sdk.values.TupleTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConcatConnector extends AbstractStepRunner implements StepRunner, Serializable {
  private static final Logger LOG = LoggerFactory.getLogger(ConcatConnector.class);
  private static final long serialVersionUID = 8007583692748290306L;

  /**
   * Connector concatenating two upstream pipelines
   *
   * @param id The Concat Connector Pipeline id
   * @param leftStep The Left Upstream Pipeline id
   * @param rightStep The Right Upstream Pipeline id
   */
  public ConcatConnector(String id, StepRunner leftStep, StepRunner rightStep) {
    super(StepType.Connector);
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

    this.stepId = id;
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
            schema
                .getFields()
                .stream()
                .map(c -> tableName + "." + "`" + c.getName() + "`")
                .iterator());
  }
}
