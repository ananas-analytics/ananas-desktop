package org.ananas.runner.api;

import org.ananas.runner.kernel.build.StepBuilderV2;
import org.ananas.runner.steprunner.CSVConnector;
import org.ananas.runner.steprunner.DefaultDataViewer;
import org.ananas.runner.steprunner.JdbcLoader;
import org.ananas.runner.steprunner.JsonConnector;
import org.ananas.runner.steprunner.SQLTransformer;

public class Main {

  public static void main(String[] args) {
    registerStepRunners();

    if (args.length == 0) {
      RestApiRoutes.initRestApi(args);
    } else {
      CliCommands.initCommandLine(args);
    }
  }

  public static void registerStepRunners() {
    // register step runners
    StepBuilderV2.register("org.ananas.source.file.csv", CSVConnector.class);
    StepBuilderV2.register("org.ananas.source.file.json", JsonConnector.class);

    StepBuilderV2.register("org.ananas.transform.sql", SQLTransformer.class);

    StepBuilderV2.register("org.ananas.destination.jdbc.mysql", JdbcLoader.class);
    StepBuilderV2.register("org.ananas.destination.jdbc.postgres", JdbcLoader.class);

    StepBuilderV2.register("org.ananas.visualization.barchart", DefaultDataViewer.class);
    StepBuilderV2.register("org.ananas.visualization.linechart", DefaultDataViewer.class);
    StepBuilderV2.register("org.ananas.visualization.bignumber", DefaultDataViewer.class);
  }
}
