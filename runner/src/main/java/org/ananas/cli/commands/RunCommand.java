package org.ananas.cli.commands;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.ananas.cli.DagRequestBuilder;
import org.ananas.runner.api.ApiResponse;
import org.ananas.runner.api.RestApiRoutes;
import org.ananas.runner.api.Services;
import org.ananas.runner.kernel.common.JsonUtil;
import org.ananas.runner.kernel.job.BeamRunner;
import org.ananas.runner.kernel.job.Job;
import org.ananas.runner.kernel.job.Runner;
import org.ananas.runner.kernel.model.DagRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import spark.Spark;

@Command(name = "run", description = "Run analytics task")
public class RunCommand implements Callable<Integer> {
  private static final Logger LOG = LoggerFactory.getLogger(RunCommand.class);

  @ParentCommand private MainCommand parent;

  @Option(
      names = {"-p", "--project"},
      description = "Ananas analytics project path",
      required = true)
  private File project;

  @Option(
      names = {"-e", "--profile"},
      description =
          "Profile yaml file, includes execution engine, and parameters (optional). By default, local Flink engine, no parameter")
  private File profile;

  @Option(
      names = {"-m", "--param"},
      description =
          "Override the parameter defined in profile, the parameter must be defined in ananas board")
  private Map<String, String> params;

  @Option(
      names = {"--port"},
      description = "Ananas server port, default 3003",
      defaultValue = "3003")
  private Integer port;

  @Option(
      names = {"-i", "--interval"},
      description = "State polling interval in seconds, default 5",
      defaultValue = "5")
  private Integer interval;

  @Parameters(description = "Id of the target steps to run (only LOADER and VIEWER)")
  private List<String> goals;

  @Override
  public Integer call() throws Exception {
    parent.handleVerbose();

    // first start a server
    System.out.printf("Server started at port %d\n", port);
    RestApiRoutes.initRestApi(new String[] {port.toString()});

    // build DagRequest
    try {
      DagRequest dagRequest = DagRequestBuilder.build(project, profile, params, goals);
      Object result = Services.runDag(null, null, JsonUtil.toJson(dagRequest));
      System.out.println(result);

      ApiResponse apiResponse = JsonUtil.fromJson(result.toString(), ApiResponse.class);

      if (apiResponse.code != 200) {
        System.err.println(apiResponse.message);
        System.exit(1);
        return 1;
      }

      Map<String, String> data = (Map<String, String>) apiResponse.data;
      String jobId = data.get("jobid");

      Runner runner = new BeamRunner();
      String state = null;
      String errMessage = null;
      while (true) {
        Job job = runner.getJob(jobId);
        if (job == null) {
          System.err.printf("Can't find job %s", jobId);
          break;
        } else {
          state = job.getResult().getLeft().toString();
          if (job.getResult().getRight() != null) {
            errMessage = job.getResult().getRight().getLocalizedMessage();
          }

          if (errMessage == null) {
            System.out.printf("Job state %s\n", state);
          } else {
            System.out.printf("Job state %s, with error: %s\n", state, errMessage);
            break;
          }

          if (state != null && (state.equals("DONE") || state.equals("FAILED"))) {
            break;
          }
        }
        try {
          Thread.sleep(interval * 1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }

      Spark.stop();
      System.exit(0);
      return 0;
    } catch (Exception e) {
      LOG.error(e.getLocalizedMessage());
      e.printStackTrace();
      System.exit(1);
      return 1;
    }
  }
}
