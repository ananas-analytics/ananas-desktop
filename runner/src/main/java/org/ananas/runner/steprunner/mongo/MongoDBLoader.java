package org.ananas.runner.steprunner.mongo;

import org.ananas.runner.kernel.LoaderStepRunner;
import org.ananas.runner.kernel.StepRunner;
import org.ananas.runner.kernel.common.DataReader;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.legacy.steps.commons.NullDataReader;
import org.ananas.runner.legacy.steps.commons.json.AsBsons;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoDBLoader extends LoaderStepRunner {

  private static final Logger LOG = LoggerFactory.getLogger(MongoDBLoader.class);
  private static final long serialVersionUID = -5336365297270280769L;

  public MongoDBLoader(Step step, StepRunner previous, boolean isTest) {
    super(step, previous, isTest);
  }

  @Override
  public DataReader getReader() {
    return NullDataReader.of();
  }

  @Override
  public Schema getSchema() {
    return Schema.builder().build();
  }

  public void build() {
    MongoStepConfig config = new MongoStepConfig(step.config);

    super.output = null;

    super.output = null;

    if (isTest) {
      return;
    }

    previous
        .getOutput()
        .apply(AsBsons.of())
        .apply(
            org.apache.beam.sdk.io.mongodb.MongoDbIO.<Row>write()
                .withUri(config.getUrl())
                .withDatabase(config.database)
                .withCollection(config.collection));
  }

  @Override
  public void setReader() {
    // NO OPER
  }
}
