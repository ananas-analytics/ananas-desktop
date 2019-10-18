package org.ananas.cli.commands;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import org.ananas.cli.DagRequestBuilder;
import org.ananas.cli.Helper;
import org.ananas.cli.commands.extension.ExtensionHelper;
import org.ananas.cli.model.AnalyticsBoard;
import org.ananas.runner.core.common.JsonUtil;
import org.ananas.runner.core.extension.DefaultExtensionManager;
import org.ananas.runner.core.extension.ExtensionManager;
import org.ananas.runner.core.extension.LocalExtensionRepository;
import org.ananas.runner.core.model.Dataframe;
import org.ananas.runner.core.model.Step;
import org.ananas.runner.core.model.StepType;
import org.ananas.runner.core.paginate.PaginationBody;
import org.ananas.runner.core.paginate.Paginator;
import org.ananas.runner.core.paginate.PaginatorFactory;
import org.ananas.runner.misc.HomeManager;
import org.ananas.server.ApiResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

@CommandLine.Command(name = "explore", description = "Explore source and destination")
public class ExploreCommand implements Callable<Integer> {
  private static final Logger LOG = LoggerFactory.getLogger(ExploreCommand.class);

  @CommandLine.ParentCommand private MainCommand parent;

  @CommandLine.Option(
      names = {"-p", "--project"},
      description = "Ananas analytics project path, default: current directory")
  private File project = new File(".");

  @CommandLine.Option(
      names = {"-m", "--param"},
      description = "Parameter values, the parameter must be defined in ananas file")
  private Map<String, String> params;

  @CommandLine.Option(
      names = {"-r", "--repo"},
      description = "Extension repository location, by default, ./extensions")
  private File repo = new File("./extensions");

  @CommandLine.Option(
      names = {"-g", "--global"},
      description = "Load extensions from global repository")
  private boolean global = false;

  @CommandLine.Option(
      names = {"-x", "--extension"},
      description = "Extension location, could be absolute path or relative to current directory")
  private List<File> extensions;

  @CommandLine.Option(
      names = {"-n"},
      description = "The page number",
      defaultValue = "0")
  private Integer n;

  @CommandLine.Option(
      names = {"-s", "--size"},
      description = "The page size",
      defaultValue = "100")
  private Integer size;

  @CommandLine.Parameters(
      index = "0",
      description = "Id of the step to explore, must be source or destination")
  private String stepId;

  @Override
  public Integer call() {
    parent.handleVerbose();

    if (!Helper.isAnanasProject(project)) {
      System.out.println("Invalid project: " + project.getAbsolutePath());
      return 1;
    }
    if (global) {
      repo = new File(HomeManager.getHomeExtensionPath());
    }
    if (ExtensionHelper.initExtensionRepository(repo, extensions) != 0) {
      return 1;
    }

    if (params == null) {
      params = new HashMap<>();
    }

    AnalyticsBoard analyticsBoard = Helper.createAnalyticsBoard(project);
    if (analyticsBoard == null) {
      return 1;
    }

    if (!analyticsBoard.steps.containsKey(stepId)) {
      System.err.printf("Can't find step %s in ananas board\n", stepId);
      return 1;
    }

    Step step = analyticsBoard.steps.get(stepId);
    if (!StepType.Connector.name().toLowerCase().equals(step.type.toLowerCase())
        && !StepType.Loader.name().toLowerCase().equals(step.type.toLowerCase())) {
      System.err.printf("Only Connector (Source) and Loader (Destination) can be explored\n");
      return 1;
    }

    PaginationBody paginationBody = new PaginationBody();
    paginationBody.type = step.type;
    paginationBody.metadataId = step.metadataId;
    paginationBody.config = step.config;
    paginationBody.dataframe = step.dataframe;
    paginationBody.params =
        analyticsBoard.variables.stream().collect(Collectors.toMap(v -> v.name, v -> v));

    try {
      paginationBody.extensions =
          ExtensionHelper.openExtensionList(new File(project, "extension.yml").getAbsolutePath());
    } catch (IOException e) {
      paginationBody.extensions = new HashMap<>();
    }

    params
        .keySet()
        .forEach(
            k -> {
              if (paginationBody.params.containsKey(k)) {
                paginationBody.params.get(k).value = params.get(k);
              }
            });

    DagRequestBuilder.injectRuntimeVariables(paginationBody.params, project.getAbsolutePath());

    try {
      ExtensionManager extensionManager =
          new DefaultExtensionManager(LocalExtensionRepository.getDefault());
      extensionManager.resolve(paginationBody.extensions);
      Paginator paginator = PaginatorFactory.of(stepId, paginationBody, extensionManager);
      Dataframe dataframe = paginator.paginate(n, size);
      System.out.println(JsonUtil.toJson(ApiResponseBuilder.Of().OK(dataframe).build()));
    } catch (Exception e) {
      System.err.println(JsonUtil.toJson(ApiResponseBuilder.Of().KO(e).build()));
      return 1;
    }

    return 0;
  }
}
