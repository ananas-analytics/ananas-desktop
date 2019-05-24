package org.ananas.runner.model.steps.join;

import com.google.common.base.Joiner;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.ananas.runner.model.steps.commons.AbstractStepRunner;
import org.ananas.runner.model.steps.commons.StepRunner;
import org.ananas.runner.model.steps.commons.StepType;
import org.apache.beam.sdk.extensions.sql.SqlTransform;
import org.apache.beam.sdk.values.PCollectionTuple;
import org.apache.beam.sdk.values.TupleTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JoinConnector extends AbstractStepRunner implements StepRunner, Serializable {
  private static final Logger LOG = LoggerFactory.getLogger(JoinConnector.class);
  private static final long serialVersionUID = 8310470332280930099L;

  /**
   * Connector joining two upstream pipelines
   *
   * @param id The Join Connector Pipeline id
   * @param joinType The {@link JoinType} name
   * @param leftStep The Left Upstream Pipeline id
   * @param rightStep The Right Upstream Pipeline id
   * @param leftColumns The left Pipeline Column projected
   * @param rightColumns The right Pipeline Column projected
   * @param columnsMap The columns mapped ( map[leftColumns]=rightColumns
   */
  public JoinConnector(
      String id,
      String joinType,
      StepRunner leftStep,
      StepRunner rightStep,
      List<String> leftColumns,
      List<String> rightColumns,
      Map<String, String> columnsMap) {
    super(StepType.Connector);

    this.stepId = id;

    JoinType join = JoinType.safeValueOf(joinType);

    PCollectionTuple collectionTuple =
        PCollectionTuple.of(new TupleTag<>("TableLeft"), leftStep.getOutput())
            .and(new TupleTag<>("TableRight"), rightStep.getOutput());

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
