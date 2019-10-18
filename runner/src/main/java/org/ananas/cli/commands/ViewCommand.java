package org.ananas.cli.commands;

import static org.ananas.runner.misc.YamlHelper.openYAML;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.Callable;
import org.ananas.cli.Helper;
import org.ananas.cli.model.AnalyticsBoard;
import org.ananas.cli.model.Profile;
import org.ananas.runner.core.common.JsonUtil;
import org.ananas.runner.core.model.Engine;
import org.ananas.runner.misc.YamlHelper;
import org.ananas.runner.steprunner.DefaultDataViewer;
import org.ananas.server.ApiResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

@CommandLine.Command(name = "view", description = "Explore data view result")
public class ViewCommand implements Callable<Integer> {
  private static final Logger LOG = LoggerFactory.getLogger(ViewCommand.class);

  @CommandLine.ParentCommand private MainCommand parent;

  @CommandLine.Option(
      names = {"-p", "--project"},
      description = "Ananas analytics project path, default: current directory")
  private File project = new File(".");

  @CommandLine.Option(
      names = {"-f", "--profile"},
      description =
          "Profile yaml file, includes execution engine, and parameters (optional). By default, local Flink engine, no parameter")
  private File profile;

  @CommandLine.Option(
      names = {"-q", "--query"},
      description = "SQL query of the result",
      defaultValue = "SELECT * FROM PCOLLECTION")
  private String sql;

  @CommandLine.Parameters(index = "0", description = "The job id")
  private String jobId;

  @CommandLine.Parameters(index = "1", description = "Id of the target step (VIEWER)")
  private String goal;

  @Override
  public Integer call() {
    parent.handleVerbose();

    if (!Helper.isAnanasProject(project)) {
      System.out.println("Invalid project: " + project.getAbsolutePath());
      return 1;
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

    if (!analyticsBoard.steps.containsKey(goal)) {
      System.err.printf("Can't find step %s in ananas board\n", goal);
      return 1;
    }

    // parse profile
    Profile profileObj = new Profile();
    try {
      if (profile.exists()) {
        profileObj = openYAML(profile.getAbsolutePath(), Profile.class);
      } else {
        LOG.error(
            String.format(
                "Profile %s not found, fallback to default Local Flink Engine\n",
                profile.getAbsolutePath()));
      }
    } catch (Exception e) {
      LOG.warn("Failed to parse profile file: " + e.getLocalizedMessage());
      LOG.warn("Fallback to default Local Flink Engine");
      Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).forEach(LOG::error);
    }

    Engine engine = profileObj.engine;

    if (engine == null) {
      engine = new Engine();
      engine.name = "Local Engine";
      engine.type = "flink";
      engine.properties = new HashMap<>();
      /*
      engine.properties.put("flinkMaster", "[auto]");
      engine.properties.put("tempLocation", "/tmp/");
      engine.properties.put("parallelism", "10");
      engine.properties.put("maxBundleSize", "1000000");
       */
      engine.properties.put("database_type", "derby");
    }

    DefaultDataViewer.DataViewRepository repository = new DefaultDataViewer.DataViewRepository();
    try {
      String output =
          JsonUtil.toJson(
              ApiResponseBuilder.Of().OK(repository.query(sql, jobId, goal, engine)).build());

      System.out.println(output);
    } catch (Exception e) {
      System.err.println(e.getLocalizedMessage());
      return 1;
    }
    return 0;
  }
}
