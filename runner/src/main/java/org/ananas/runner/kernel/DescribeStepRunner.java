package org.ananas.runner.kernel;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.steprunner.sql.SQLTransformer;
import org.apache.beam.sdk.schemas.Schema;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Transformer that describes the previous step dataframe columns that are numeric.
 */
public class DescribeStepRunner extends TransformerStepRunner {

  private static final long serialVersionUID = 4839750924289849371L;

  private static String[] aggregations = new String[] {"COUNT", "MIN", "MAX", "AVG", "VAR_POP", "VAR_SAMP"};

  ConcatStepRunner concatStepRunner;

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
    for (String aggregation : aggregations) {
        Step step = Step.of(UUID.randomUUID().toString(), null);
        step.config.put(
                  SQLTransformer.CONFIG_SQL, "SELECT "
                + SQLProjection("PCOLLECTION", previousSchema, aggregation )
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

  private String SQLProjection(String tableName, Schema schema, String aggregationFn) {
    return String.format(" '%s' as aggregation ", aggregationFn) + "," + Joiner.on(" , ")
        .join(
            schema.getFields().stream()
                    .filter( field -> field.getType().getTypeName().isNumericType() )
                .map(c -> String.format("CAST(%s(%s.`%s`) AS %s) AS %s", aggregationFn, tableName, c.getName(), "DOUBLE", c.getName().replaceAll("[^a-zA-Z]+","")))
                .iterator());
  }




}
