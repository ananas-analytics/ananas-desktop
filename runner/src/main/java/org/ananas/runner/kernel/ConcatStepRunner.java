package org.ananas.runner.kernel;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import java.util.UUID;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.kernel.model.StepType;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.RowCoderGenerator;
import org.apache.beam.sdk.extensions.sql.SqlTransform;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.schemas.Schema.Builder;
import org.apache.beam.sdk.schemas.SchemaCoder;
import org.apache.beam.sdk.transforms.Flatten;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;
import org.apache.beam.sdk.values.PCollectionTuple;
import org.apache.beam.sdk.values.Row;
import org.apache.beam.sdk.values.TupleTag;

public class ConcatStepRunner extends AbstractStepRunner {

  private static final long serialVersionUID = 4839750924289849371L;

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
    //leftStep.getSchema().setUUID(null);
    //rightStep.getSchema().setUUID(null);

    /*
    if (!leftStep.getSchema().equals(rightStep.getSchema())) {
      throw new RuntimeException("Both steps should have same columns (name and type). ");
    }
     */

    Builder outputSchemaBuilder = new Schema.Builder();
    leftStep.getSchema().getFields().forEach(field -> {
      outputSchemaBuilder.addField(field.getName(), field.getType());
    });

    Schema newSchema = outputSchemaBuilder.build();
    newSchema.setUUID(UUID.randomUUID());

    Coder<Row> coder = SchemaCoder.of(newSchema);

    this.output = PCollectionList.of(leftStep.getOutput().setCoder(coder))
      .and(rightStep.getOutput().setCoder(coder))
        .apply(Flatten.pCollections())
        .setCoder(coder);

  }

  private String SQLProjection(String tableName, Schema schema) {
    return Joiner.on(" , ")
        .join(
            schema.getFields().stream()
                .map(c -> tableName + "." + "`" + c.getName() + "`")
                .iterator());
  }
}
