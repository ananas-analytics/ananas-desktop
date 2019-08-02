package org.ananas.runner.kernel;

import org.ananas.runner.kernel.build.StepBuilder;
import org.ananas.runner.kernel.paginate.AutoDetectedSchemaPaginator;
import org.ananas.runner.kernel.paginate.PaginatorFactory;
import org.ananas.runner.paginator.BigqueryPaginator;
import org.ananas.runner.paginator.files.CSVPaginator;
import org.ananas.runner.paginator.files.GCSPaginator;
import org.ananas.runner.paginator.files.JdbcPaginator;
import org.ananas.runner.paginator.files.JsonPaginator;
import org.ananas.runner.steprunner.DefaultDataViewer;
import org.ananas.runner.steprunner.api.APIConnector;
import org.ananas.runner.steprunner.api.APIPaginator;
import org.ananas.runner.steprunner.files.FileLoader;
import org.ananas.runner.steprunner.files.JsonConnector;
import org.ananas.runner.steprunner.files.csv.CSVConnector;
import org.ananas.runner.steprunner.gcs.BigQueryConnector;
import org.ananas.runner.steprunner.gcs.BigQueryLoader;
import org.ananas.runner.steprunner.gcs.GCSConnector;
import org.ananas.runner.steprunner.gcs.GCSLoader;
import org.ananas.runner.steprunner.jdbc.JdbcConnector;
import org.ananas.runner.steprunner.jdbc.JdbcLoader;
import org.ananas.runner.steprunner.sql.SQLTransformer;

public class ExtensionRegistry {

  public static void registerConnector(
      String metaId,
      Class<? extends StepRunner> stepRunnerClass,
      Class<? extends AutoDetectedSchemaPaginator> paginatorClass) {
    StepBuilder.register(metaId, stepRunnerClass);
    PaginatorFactory.register(metaId, paginatorClass);
  }

  public static void registerTransformer(
      String metaId, Class<? extends StepRunner> stepRunnerClass) {
    StepBuilder.register(metaId, stepRunnerClass);
  }

  public static void registerLoader(
      String metaId,
      Class<? extends StepRunner> stepRunnerClass,
      Class<? extends AutoDetectedSchemaPaginator> paginatorClass) {
    StepBuilder.register(metaId, stepRunnerClass);
    PaginatorFactory.register(metaId, paginatorClass);
  }

  public static void registerViewer(String metaId, Class<? extends StepRunner> stepRunnerClass) {
    StepBuilder.register(metaId, stepRunnerClass);
  }

  public static void init() {
    ExtensionRegistry.registerConnector(
        "org.ananas.source.file.csv", CSVConnector.class, CSVPaginator.class);
    ExtensionRegistry.registerConnector(
        "org.ananas.source.file.json", JsonConnector.class, JsonPaginator.class);
    ExtensionRegistry.registerConnector(
        "org.ananas.source.file.gcs", GCSConnector.class, GCSPaginator.class);
    ExtensionRegistry.registerConnector(
        "org.ananas.source.gcp.bigquery", BigQueryConnector.class, BigqueryPaginator.class);
    ExtensionRegistry.registerConnector(
        "org.ananas.source.jdbc.mysql", JdbcConnector.class, JdbcPaginator.class);
    ExtensionRegistry.registerConnector(
        "org.ananas.source.jdbc.postgres", JdbcConnector.class, JdbcPaginator.class);
    ExtensionRegistry.registerConnector(
        "org.ananas.source.api", APIConnector.class, APIPaginator.class);

    ExtensionRegistry.registerTransformer("org.ananas.transform.sql", SQLTransformer.class);

    ExtensionRegistry.registerLoader(
        "org.ananas.destination.file.csv", FileLoader.class, CSVPaginator.class);
    ExtensionRegistry.registerLoader(
        "org.ananas.destination.jdbc.mysql", JdbcLoader.class, JdbcPaginator.class);
    ExtensionRegistry.registerLoader(
        "org.ananas.destination.jdbc.postgres", JdbcLoader.class, JdbcPaginator.class);
    ExtensionRegistry.registerLoader(
        "org.ananas.destination.gcp.gcs", GCSLoader.class, GCSPaginator.class);
    ExtensionRegistry.registerLoader(
        "org.ananas.destination.gcp.bigquery", BigQueryLoader.class, BigqueryPaginator.class);

    ExtensionRegistry.registerViewer("org.ananas.visualization.barchart", DefaultDataViewer.class);
    ExtensionRegistry.registerViewer("org.ananas.visualization.piechart", DefaultDataViewer.class);
    ExtensionRegistry.registerViewer("org.ananas.visualization.linechart", DefaultDataViewer.class);
    ExtensionRegistry.registerViewer("org.ananas.visualization.bignumber", DefaultDataViewer.class);
  }
}
