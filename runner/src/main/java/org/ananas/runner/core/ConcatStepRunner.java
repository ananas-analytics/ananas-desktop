package org.ananas.runner.core;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import java.util.List;
import java.util.UUID;
import org.ananas.runner.core.model.Step;
import org.ananas.runner.core.model.StepType;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.extensions.sql.SqlTransform;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.schemas.Schema.Builder;
import org.apache.beam.sdk.schemas.SchemaCoder;
import org.apache.beam.sdk.transforms.Flatten;
import org.apache.beam.sdk.values.PCollectionList;
import org.apache.beam.sdk.values.Row;

public class ConcatStepRunner extends AbstractStepRunner {

  private static final long serialVersionUID = 4839750924289849371L;

  public transient Step step;
  public transient List<StepRunner> upstreams;

  public ConcatStepRunner(Step step, List<StepRunner> upstreams) {
    super(StepType.Connector);

    this.stepId = step.id;

    this.step = step;

    this.upstreams = upstreams;
  }

  public void build() {
    Preconditions.checkNotNull(upstreams);

    if (upstreams.size() == 0) {
      throw new RuntimeException(
          "Please connect steps to concat step. Can't find any upstream steps");
    }

    // hack for flink issue with Schema coder equals
    // leftStep.getSchema().setUUID(null);
    // rightStep.getSchema().setUUID(null);

    /*
    if (!leftStep.getSchema().equals(rightStep.getSchema())) {
      throw new RuntimeException("Both steps should have same columns (name and type). ");
    }
     */

    UUID inputSchemaUUID = UUID.randomUUID();
    upstreams.forEach(
        stepRunner -> {
          stepRunner.getSchema().setUUID(null);
          stepRunner.getSchema().setUUID(inputSchemaUUID);
        });

    Builder outputSchemaBuilder = new Schema.Builder();
    upstreams
        .get(0)
        .getSchema()
        .getFields()
        .forEach(
            field -> {
              outputSchemaBuilder.addField(field.getName(), field.getType());
            });

    Schema newSchema = outputSchemaBuilder.build();
    newSchema.setUUID(inputSchemaUUID);

    Coder<Row> coder = SchemaCoder.of(newSchema);

    PCollectionList<Row> pcollectionList =
        PCollectionList.of(upstreams.get(0).getOutput().setCoder(coder));

    for (int i = 1; i < upstreams.size(); i++) {
      pcollectionList = pcollectionList.and(upstreams.get(i).getOutput().setCoder(coder));
    }

    this.output =
        pcollectionList
            .apply(Flatten.pCollections())
            // TODO: better way to fix chaining multiple concat 'Flink Cannot union streams of
            // different types issue'
            .apply("sql transform", SqlTransform.query("SELECT * FROM PCOLLECTION"))
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
