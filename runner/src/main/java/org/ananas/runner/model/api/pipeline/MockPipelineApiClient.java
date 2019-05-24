package org.ananas.runner.model.api.pipeline;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.ananas.runner.model.core.Pipeline;
import org.ananas.runner.model.core.Step;
import org.ananas.runner.model.core.StepConfig;

public class MockPipelineApiClient {

  public Pipeline getPipeline(String id) {

    Step connector = new Step();
    connector.id = "122J3K";
    connector.name = "read_csv";
    connector.type = "connector";
    connector.description = "Read the score CSV";

    Map<String, Object> config = new HashMap<>();
    config.put(StepConfig.SUBTYPE, "csv");
    config.put(
        StepConfig.PATH, "/home/grego/go/src/github.com/bhou/bravetroops/api/tests/score.csv");
    config.put(StepConfig.IS_HEADER, true);

    connector.config = config;

    Step transformer = new Step();
    transformer.id = "234122J3K";
    transformer.name = "sql transformer";
    transformer.type = "transformer";
    transformer.description = "SQL  transformer";

    Map<String, Object> SQLconfig = new HashMap<>();
    SQLconfig.put(StepConfig.SUBTYPE, "sql");
    SQLconfig.put(
        StepConfig.SQL,
        "SELECT max(score) as meilleurnote, min(score) as pirenote FROM PCOLLECTION");
    transformer.config = SQLconfig;

    Pipeline pipeline = new Pipeline();
    pipeline.description = "test pipelines";
    pipeline.name = "read_csv";
    pipeline.id = id;

    LinkedList steps = new LinkedList<>();
    steps.add(connector);
    steps.add(transformer);
    pipeline.steps = steps;

    return pipeline;
  }
}
