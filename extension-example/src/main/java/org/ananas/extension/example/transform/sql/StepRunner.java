package org.ananas.extension.example.transform.sql;

import org.ananas.runner.core.TransformerStepRunner;
import org.ananas.runner.core.model.Step;
import org.apache.beam.sdk.extensions.sql.SqlTransform;

public class StepRunner extends TransformerStepRunner {
  protected StepRunner(Step step, org.ananas.runner.core.StepRunner previous) {
    super(step, previous);
  }

  @Override
  public void build() {
    String statement = (String) this.step.config.get("sql");
    this.output =
      previous
        .getOutput()
        .apply(
          "simple sql transform",
          SqlTransform.query(statement));
  }
}
