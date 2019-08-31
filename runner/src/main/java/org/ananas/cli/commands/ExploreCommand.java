package org.ananas.cli.commands;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import org.ananas.cli.DagRequestBuilder;
import org.ananas.cli.YamlHelper;
import org.ananas.cli.model.AnalyticsBoard;
import org.ananas.runner.core.common.JsonUtil;
import org.ananas.runner.core.model.Dataframe;
import org.ananas.runner.core.model.Step;
import org.ananas.runner.core.model.StepType;
import org.ananas.runner.core.paginate.PaginationBody;
import org.ananas.runner.core.paginate.Paginator;
import org.ananas.runner.core.paginate.PaginatorFactory;
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
      description = "Ananas analytics project path",
      required = true)
  private File project;

  @CommandLine.Option(
      names = {"-m", "--param"},
      description = "Parameter values, the parameter must be defined in ananas file")
  private Map<String, String> params;

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

    if (params == null) {
      params = new HashMap<>();
    }

    AnalyticsBoard analyticsBoard = null;
    File ananas = Paths.get(project.getAbsolutePath(), "ananas.yml").toFile();
    if (!ananas.exists()) {
      ananas = Paths.get(project.getAbsolutePath(), "ananas.yaml").toFile();
      if (!ananas.exists()) {
        System.err.println("Can't find ananas.yml file in your project");
        return 1;
      }
    }
    try {
      analyticsBoard = YamlHelper.openYAML(ananas.getAbsolutePath(), AnalyticsBoard.class);
    } catch (Exception e) {
      System.err.println("Failed to parse analytics board file: " + e.getLocalizedMessage());
      Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).forEach(LOG::error);
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
      Paginator paginator = PaginatorFactory.of(stepId, paginationBody);
      Dataframe dataframe = paginator.paginate(n, size);
      System.out.println(JsonUtil.toJson(ApiResponseBuilder.Of().OK(dataframe).build()));
    } catch (Exception e) {
      System.err.println(JsonUtil.toJson(ApiResponseBuilder.Of().KO(e).build()));
      return 1;
    }

    return 0;
  }
}
