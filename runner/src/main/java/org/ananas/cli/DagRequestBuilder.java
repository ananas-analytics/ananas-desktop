package org.ananas.cli;

import static org.ananas.runner.misc.YamlHelper.openYAML;

import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.ananas.cli.model.AnalyticsBoard;
import org.ananas.cli.model.Profile;
import org.ananas.runner.core.model.*;
import org.ananas.runner.misc.YamlHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DagRequestBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(DagRequestBuilder.class);

  public static DagRequest build(
      File project, File profile, Map<String, String> params, List<String> goals) {
    // create dag request placeholder object
    DagRequest dagRequest = new DagRequest();
    dagRequest.dag = new Dag();

    if (params == null) {
      params = new HashMap<>();
    }

    // parse ananas board file
    AnalyticsBoard analyticsBoard = null;
    File ananas = Paths.get(project.getAbsolutePath(), "ananas.yml").toFile();
    if (!ananas.exists()) {
      ananas = Paths.get(project.getAbsolutePath(), "ananas.yaml").toFile();
      if (!ananas.exists()) {
        throw new RuntimeException(
            "Can't find ananas.yml file in your project: " + project.getAbsolutePath());
      }
    }

    try {
      analyticsBoard = openYAML(ananas.getAbsolutePath(), AnalyticsBoard.class);
    } catch (Exception e) {
      throw new RuntimeException(
          "Failed to parse analytics board file: " + e.getLocalizedMessage(), e);
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

    if (profileObj.engine == null) {
      profileObj.engine = new Engine();
      profileObj.engine.name = "Local Engine";
      profileObj.engine.type = "flink";
      profileObj.engine.properties = new HashMap<>();
      profileObj.engine.properties.put("database_type", "derby");
    }

    // parse extension.yml
    File extensionFile = Paths.get(project.getAbsolutePath(), "extention.yml").toFile();
    Map<String, Extension> extensions = new HashMap<>();
    if (extensionFile.exists()) {
      try {
        extensions = YamlHelper.openMapYAML(extensionFile.getAbsolutePath(), Extension.class);
      } catch (IOException e) {
        LOG.error("Failed to parse extension file: " + e.getLocalizedMessage());
      }
    }

    // construct dag request
    dagRequest.dag.connections = analyticsBoard.dag.connections;
    dagRequest.dag.steps = Sets.newHashSet(analyticsBoard.steps.values());
    dagRequest.engine = profileObj.engine;
    dagRequest.extensions = extensions;
    dagRequest.goals = new HashSet<>(goals);
    dagRequest.params =
        analyticsBoard.variables.stream().collect(Collectors.toMap(v -> v.name, v -> v));
    profileObj.params.forEach(
        (k, v) -> {
          if (dagRequest.params.containsKey(k)) {
            dagRequest.params.get(k).value = v;
          }
        });
    params.forEach(
        (k, v) -> {
          if (dagRequest.params.containsKey(k)) {
            dagRequest.params.get(k).value = v;
          }
        });

    // inject runtime variables
    injectRuntimeVariables(dagRequest.params, project.getAbsolutePath());

    return dagRequest;
  }

  public static void injectRuntimeVariables(Map<String, Variable> variables, String projectPath) {
    variables.put(
        "EXECUTE_TIME",
        new Variable("EXECUTE_TIME", "date", "", "runtime", "" + System.currentTimeMillis()));
    variables.put(
        "PROJECT_PATH", new Variable("PROJECT_PATH", "string", "", "runtime", projectPath));
  }
}
