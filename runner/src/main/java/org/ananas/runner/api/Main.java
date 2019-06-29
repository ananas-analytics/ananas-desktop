package org.ananas.runner.api;

import org.ananas.runner.kernel.ExtensionRegistry;
import org.ananas.runner.paginator.files.CSVPaginator;
import org.ananas.runner.paginator.files.GCSPaginator;
import org.ananas.runner.paginator.files.JdbcPaginator;
import org.ananas.runner.steprunner.DefaultDataViewer;
import org.ananas.runner.steprunner.files.FileLoader;
import org.ananas.runner.steprunner.files.csv.CSVConnector;
import org.ananas.runner.steprunner.files.gcs.GCSConnector;
import org.ananas.runner.steprunner.jdbc.JdbcConnector;
import org.ananas.runner.steprunner.jdbc.JdbcLoader;
import org.ananas.runner.steprunner.sql.SQLTransformer;

public class Main {

  public static void main(String[] args) {
    registerExtensions();

    if (args.length == 0) {
      RestApiRoutes.initRestApi(args);
    } else {
      CliCommands.initCommandLine(args);
    }
  }

  static void registerExtensions() {
    ExtensionRegistry.registerConnector(
        "org.ananas.source.file.csv", CSVConnector.class, CSVPaginator.class);
    ExtensionRegistry.registerConnector(
        "org.ananas.source.file.gcs", GCSConnector.class, GCSPaginator.class);
    ExtensionRegistry.registerConnector(
        "org.ananas.source.jdbc.mysql", JdbcConnector.class, JdbcPaginator.class);
    ExtensionRegistry.registerConnector(
        "org.ananas.source.jdbc.postgres", JdbcConnector.class, JdbcPaginator.class);

    ExtensionRegistry.registerTransformer("org.ananas.transform.sql", SQLTransformer.class);

    ExtensionRegistry.registerLoader(
        "org.ananas.destination.file.csv", FileLoader.class, CSVPaginator.class);
    ExtensionRegistry.registerLoader(
        "org.ananas.destination.jdbc.mysql", JdbcLoader.class, JdbcPaginator.class);
    ExtensionRegistry.registerLoader(
        "org.ananas.destination.jdbc.postgres", JdbcLoader.class, JdbcPaginator.class);

    ExtensionRegistry.registerViewer("org.ananas.visualization.barchart", DefaultDataViewer.class);
    ExtensionRegistry.registerViewer("org.ananas.visualization.linechart", DefaultDataViewer.class);
    ExtensionRegistry.registerViewer("org.ananas.visualization.bignumber", DefaultDataViewer.class);
  }
}
