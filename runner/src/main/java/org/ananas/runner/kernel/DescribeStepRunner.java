package org.ananas.runner.kernel;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.steprunner.sql.SQLTransformer;
import org.apache.beam.sdk.schemas.Schema;
import org.spark_project.guava.collect.ImmutableMap;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Transformer that describes the previous step dataframe columns that are numeric.
 */
public class DescribeStepRunner extends TransformerStepRunner {

  private static final long serialVersionUID = 4839750924289849371L;

  private static Map<String, LambdaExpression> aggregations = ImmutableMap.<String, LambdaExpression>builder()
          .put("count",  str -> String.format("CAST(COUNT(%s) AS DOUBLE)",str))
          .put("min",  str -> String.format("CAST(MIN(%s) AS DOUBLE)",str))
          .put("max",  str -> String.format("CAST(MAX(%s) AS DOUBLE)",str))
          .put("avg",  str -> String.format("CAST(AVG(%s) AS DOUBLE)",str))
          .put("std",  str -> String.format("CAST(SQRT(VAR_POP(%s)) AS DOUBLE)",str)).build();

  ConcatStepRunner concatStepRunner;

  public interface LambdaExpression {
    String expression(String fieldName);
  }

  public DescribeStepRunner(Step step, StepRunner previous) {

    super(step, previous);
  }

  public void build() {
    Preconditions.checkNotNull(previous);

    List<StepRunner> stepRunners = new LinkedList<>();
    Schema previousSchema = previous.getSchema();

    if (!previousSchema.getFields().stream().filter(field -> field.getType().getTypeName().isNumericType()).findFirst().isPresent()) {
      throw new RuntimeException("Oops ! There is no numeric column.");
    }
    for (Map.Entry<String, LambdaExpression> aggregation : aggregations.entrySet()) {
        Step step = Step.of(UUID.randomUUID().toString(), null);
        step.config.put(
                  SQLTransformer.CONFIG_SQL, "SELECT "
                + SQLProjection("PCOLLECTION", previousSchema, aggregation.getKey(), aggregation.getValue() )
                + " FROM PCOLLECTION ");
        StepRunner stepRunner = new SQLTransformer(step, previous);
        stepRunner.build();
        stepRunners.add(stepRunner);
    }

    Step step = Step.of(UUID.randomUUID().toString(), null);
    concatStepRunner = new ConcatStepRunner(step, stepRunners);
    concatStepRunner.build();
    this.setOutput(concatStepRunner.getOutput());
    this.setOutputMessage(concatStepRunner.getMessage());
    this.setSchemaCoder(concatStepRunner.getSchemaCoder());
  }

  @Override
  public Schema getSchema() {
    return concatStepRunner.getSchema();
  }


  private String SQLProjection(String tableName, Schema schema, String aggregationFn, LambdaExpression exp) {
    return String.format(" '%s' as aggregation ", aggregationFn) + ","
    + Joiner.on(" , ")
        .join(
            schema.getFields().stream()
                    .filter( field -> field.getType().getTypeName().isNumericType() )
                .map(c -> String.format("%s AS %s", exp.expression(String.format("%s.`%s`", tableName, c.getName())) , c.getName().replaceAll("[^a-zA-Z]+","")))
                .iterator());
  }


}
