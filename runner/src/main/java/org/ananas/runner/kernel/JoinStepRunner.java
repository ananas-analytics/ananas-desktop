package org.ananas.runner.kernel;

import com.google.common.base.Joiner;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.kernel.model.StepType;
import org.ananas.runner.kernel.schema.SchemaField;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.extensions.sql.SqlTransform;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.schemas.Schema.Builder;
import org.apache.beam.sdk.schemas.Schema.Field;
import org.apache.beam.sdk.schemas.SchemaCoder;
import org.apache.beam.sdk.values.PCollectionTuple;
import org.apache.beam.sdk.values.Row;
import org.apache.beam.sdk.values.TupleTag;

public class JoinStepRunner extends AbstractStepRunner {

  private static final long serialVersionUID = 5288723478526285311L;

  public static final String JOIN_LEFT_STEPID = "leftstepid";
  public static final String JOIN_MAP = "joinedcolumnmap";
  public static final String JOIN_TYPE = "jointype";
  public static final String JOIN_LEFT_COLUMNS = "leftcolumns";
  public static final String JOIN_RIGHT_COLUMNS = "rightcolumns";

  protected transient Step step;
  protected transient StepRunner one;
  protected transient StepRunner another;

  public enum JoinType {
    LEFT_JOIN("leftjoin", "LEFT JOIN");

    public String name;

    private String sqlExpression;

    JoinType(String name, String sqlExpression) {
      this.name = name;
      this.sqlExpression = sqlExpression;
    }

    public static JoinType safeValueOf(String literal) {
      for (JoinType j : JoinType.values()) {
        if (j.name.toLowerCase().equals(literal == null ? "" : literal.toLowerCase())) {
          return j;
        }
      }
      throw new RuntimeException(
          "'" + literal + "' join does not exit. Choose one of them : " + JoinType.values());
    }
  }

  public JoinStepRunner(Step step, StepRunner one, StepRunner another) {
    super(StepType.Transformer);

    this.stepId = step.id;

    this.step = step;
    this.one = one;
    this.another = another;
  }

  public void build() {
    String leftStepId = (String) step.config.get(JOIN_LEFT_STEPID);
    StepRunner leftStep, rightStep;
    if (one.getStepId().equals(leftStepId)) {
      leftStep = one;
      rightStep = another;
    } else {
      leftStep = another;
      rightStep = one;
    }
    Map<String, String> columnsMap = (Map) step.config.get(JOIN_MAP);
    String joinType =
        (String) step.config.getOrDefault(JOIN_TYPE, JoinStepRunner.JoinType.LEFT_JOIN.name);
    List<String> leftColumns = (List) step.config.get(JOIN_LEFT_COLUMNS);
    List<String> rightColumns = (List) step.config.get(JOIN_RIGHT_COLUMNS);

    JoinType join = JoinType.safeValueOf(joinType);

    Schema leftSchema = normalizeSchema(leftStep.getSchema());
    Schema rightSchema = normalizeSchema(rightStep.getSchema());

    Coder<Row> leftCoder = SchemaCoder.of(leftSchema);
    Coder<Row> rightCoder = SchemaCoder.of(rightSchema);


    PCollectionTuple collectionTuple =
        PCollectionTuple.of(new TupleTag<>("TableLeft"), leftStep.getOutput().setCoder(leftCoder))
            .and(new TupleTag<>("TableRight"), rightStep.getOutput().setCoder(rightCoder));

    this.output =
        collectionTuple.apply(
            SqlTransform.query(
                "SELECT "
                    + SQLProjection("TableLeft", leftColumns)
                    + ", "
                    + SQLProjection("TableRight", rightColumns)
                    + " FROM TableLeft "
                    + join.sqlExpression
                    + " TableRight ON "
                    + SQLJoin(columnsMap)));
  }

  private Schema normalizeSchema(Schema schema) {
    Builder builder = new Schema.Builder();
    schema.getFields().forEach(field -> {
      if (field.getType().getLogicalType() != null) {
        Field f = SchemaField.Of(field.getName(), field.getType()).toBeamField();
        builder.addField(f.getName(), f.getType());
      } else {
        builder.addField(field.getName(), field.getType());
      }
    });
    return builder.build();
  }

  private String SQLProjection(String tableName, List<String> columns) {
    return Joiner.on(" , ")
        .join(columns.stream().map(c -> tableName + "." + "`" + c + "`").iterator());
  }

  private String SQLJoin(Map<String, String> joinedColumns) {
    return Joiner.on(" AND ")
        .join(
            joinedColumns.entrySet().stream()
                .map(
                    e ->
                        String.format(
                            "CAST(TableLeft.`%s` as VARCHAR) = CAST(TableRight.`%s` as VARCHAR)",
                            e.getKey(), e.getValue()))
                .iterator());
  }
}
