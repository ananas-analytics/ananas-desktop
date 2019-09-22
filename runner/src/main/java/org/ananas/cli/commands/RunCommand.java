package org.ananas.cli.commands;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.ananas.cli.DagRequestBuilder;
import org.ananas.cli.commands.extension.ExtensionHelper;
import org.ananas.runner.core.common.JsonUtil;
import org.ananas.runner.core.job.BeamRunner;
import org.ananas.runner.core.job.Job;
import org.ananas.runner.core.job.Runner;
import org.ananas.runner.core.model.DagRequest;
import org.ananas.server.ApiResponse;
import org.ananas.server.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

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
      names = {"-f", "--profile"},
      description =
          "Profile yaml file, includes execution engine, and parameters (optional). By default, local Flink engine, no parameter")
  private File profile;

  @Option(
      names = {"-m", "--param"},
      description =
          "Override the parameter defined in profile, the parameter must be defined in ananas board")
  private Map<String, String> params;

  @Option(
      names = {"-r", "--repo"},
      description = "Extension repository location, by default, <ANANAS_WORKSPACE>/extensions")
  private File repo;

  @Option(
      names = {"-x", "--extension"},
      description = "Extension location, could be absolute path or relative to current directory")
  private List<File> extensions;

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

    if (ExtensionHelper.loadExtensions(repo, extensions) != 0) {
      return 1;
    }

    // build DagRequest
    try {
      DagRequest dagRequest = DagRequestBuilder.build(project, profile, params, goals);
      DagRequest resolvedDagRequest = dagRequest.resolveVariables();
      Object result = Services.runDag(null, null, JsonUtil.toJson(resolvedDagRequest));
      System.out.println(result);

      ApiResponse apiResponse = JsonUtil.fromJson(result.toString(), ApiResponse.class);

      if (apiResponse.code != 200) {
        System.err.println(apiResponse.message);
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
          // System.err.printf("Can't find job %s", jobId);
          errMessage = "Can't find job " + jobId;
          break;
        } else {
          state = job.getResult().getLeft().toString();
          if (job.getResult().getRight() != null) {
            errMessage = job.getResult().getRight().getLocalizedMessage();
          }

          if (errMessage != null) {
            break;
          }

          if (state != null
              && (state.equalsIgnoreCase("DONE") || state.equalsIgnoreCase("FAILED"))) {
            break;
          }
        }
        try {
          Thread.sleep(interval * 1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
          throw e;
        }
      }

      if (errMessage != null) {
        return 1;
      }
      return 0;
    } catch (Exception e) {
      LOG.error(e.getLocalizedMessage());
      e.printStackTrace();
      return 1;
    }
  }
}
