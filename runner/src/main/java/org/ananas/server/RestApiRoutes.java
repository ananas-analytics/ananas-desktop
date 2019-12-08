package org.ananas.server;

import static spark.Spark.*;

import org.ananas.runner.core.extension.LocalExtensionRepository;
import org.ananas.runner.misc.BackgroundApiService;

/** REST API Routes */
public class RestApiRoutes {

  public static void initRestApi(String host, int port) {
    int maxThreads = 8;
    int minThreads = 2;
    int timeOutMillis = 30000;
    threadPool(maxThreads, minThreads, timeOutMillis);

    ipAddress(host);
    port(port);

    // expose repository as a static server
    staticFiles.externalLocation(LocalExtensionRepository.getDefault().getRepositoryRoot());
    staticFiles.expireTime(600); // cache 10 minutes

    // CORS
    options(
        "/*",
        (request, response) -> {
          String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
          if (accessControlRequestHeaders != null) {
            response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
          }

          String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
          if (accessControlRequestMethod != null) {
            response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
          }

          return "OK";
        });

    before(
        (request, response) -> {
          response.header("Access-Control-Allow-Origin", "*");
          response.header("Access-Control-Request-Method", "POST");
          response.header("Access-Control-Allow-Headers", "");
          // Note: this may or may not be necessary in your particular application
          response.type("application/json");
        });

    // Endpoints
    post("/v1/:id/paginate", HttpHandler.paginateStep);

    post("/v1/:projectid/dag/test", HttpHandler.testDag);
    post("/v1/:projectid/dag/run", HttpHandler.runDag);

    get("/v1/jobs/:jobid/poll", HttpHandler.pollJob);
    get("/v1/jobs/", HttpHandler.listJobs);
    get("/v1/goal/:goalid/jobs", HttpHandler.getJobsByGoal);
    post("/v1/jobs/:id/cancel", HttpHandler.cancelPipeline);

    get("/v1/data/:jobid/:stepid", HttpHandler.dataView);

    post("/v1/extensions/metadata", HttpHandler.extensionMetadata);
    get("/v1/extension/:name/:version/editors", HttpHandler.allEditors);
    get("/v1/extension/:name/:version/editor/:metadataId", HttpHandler.editor);

    // Exception handler
    exception(Exception.class, HttpHandler.error);

    // background services
    BackgroundApiService backgroundService = new BackgroundApiService();
    Thread deamonthread = new Thread(backgroundService);
    deamonthread.setDaemon(true);
    deamonthread.start();

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              @Override
              public void run() {
                backgroundService.doStop();
                backgroundService.cancelRunningJobs();
              }
            });
  }
}
