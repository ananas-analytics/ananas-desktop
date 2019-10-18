package org.ananas.cli.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.ananas.cli.CommandLineTable;
import org.ananas.cli.Helper;
import org.ananas.cli.model.AnalyticsBoard;
import org.ananas.runner.core.common.JsonUtil;
import org.ananas.runner.core.model.Dataframe;
import org.ananas.runner.core.model.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Command(name = "show", description = "Show analytics board details")
public class ShowCommand implements Callable<Integer> {
  private static final Logger LOG = LoggerFactory.getLogger(ShowCommand.class);
  @ParentCommand private MainCommand parent;

  @Option(
      names = {"-p", "--project"},
      description = "Ananas analytics project path, default: current directory")
  private File project = new File(".");

  @Option(
      names = {"--steps"},
      description = "Show steps")
  private boolean steps;

  @Option(
      names = {"--variables"},
      description = "Show variables")
  private boolean variables;

  @Option(
      names = {"--step"},
      description = "Specify the step",
      paramLabel = "stepId")
  private String stepId;

  @Option(
      names = {"--dataframe"},
      description = "Show step dataframe")
  private boolean showDataframe;

  @Option(
      names = {"--config"},
      description = "Show step config")
  private boolean showStepConfig;

  @Option(
      names = {"--json"},
      description = "Show result in JSON format, default: false")
  private boolean json = false;

  @Override
  public Integer call() {
    parent.handleVerbose();

    if (!Helper.isAnanasProject(project)) {
      System.out.println("Invalid project: " + project.getAbsolutePath());
      return 1;
    }

    AnalyticsBoard analyticsBoard = Helper.createAnalyticsBoard(project);
    if (analyticsBoard == null) {
      return 1;
    }

    boolean didSomething = false;

    if (steps) {
      didSomething = true;
      if (json) {
        List<Map<String, String>> jsonSteps = new ArrayList<>();
        analyticsBoard.steps.forEach(
            (id, step) -> {
              Map<String, String> jsonStep = new HashMap<>();
              jsonStep.put("id", id);
              jsonStep.put("type", step.type.toUpperCase());
              jsonStep.put("name", step.name);
              jsonSteps.add(jsonStep);
            });
        System.out.println(JsonUtil.toJson(jsonSteps));
      } else {
        System.out.println("STEPS");
        CommandLineTable st = new CommandLineTable();
        // st.setRightAlign(true);//if true then cell text is right aligned
        st.setShowVerticalLines(true); // if false (default) then no vertical lines are shown
        st.setHeaders(
            "ID", "TYPE",
            "NAME"); // optional - if not used then there will be no header and horizontal lines
        analyticsBoard.steps.forEach(
            (id, step) -> {
              st.addRow(id, step.type.toUpperCase(), step.name);
            });
        st.print();
      }
    }

    if (variables) {
      didSomething = true;
      if (json) {
        System.out.println(JsonUtil.toJson(analyticsBoard.variables));
      } else {
        System.out.println("VARIABLES");
        CommandLineTable st = new CommandLineTable();
        st.setShowVerticalLines(true); // if false (default) then no vertical lines are shown
        st.setHeaders(
            "NAME",
            "TYPE",
            "DESCRIPTION"); // optional - if not used then there will be no header and horizontal
        // lines
        analyticsBoard.variables.forEach(
            v -> {
              st.addRow(
                  v.name,
                  v.type.toUpperCase(),
                  v.description.length() <= 25
                      ? v.description
                      : v.description.substring(0, 24) + "...");
            });
        st.print();
      }
    }

    if (showDataframe) {
      if (stepId == null) {
        System.err.println("Please specify the step with --step option");
        return 1;
      }

      didSomething = true;
      if (!analyticsBoard.steps.containsKey(stepId)) {
        System.err.printf("No step with id %s found in analytics board\n", stepId);
        System.err.printf(
            "Try \"ananas show -p '%s' --steps\" to get a step list.\n", project.getAbsolutePath());
        return 1;
      }

      Step step = analyticsBoard.steps.get(stepId);
      Dataframe dataframe = step.dataframe;

      if (dataframe == null || dataframe.schema == null) {
        System.err.println("No dataframe found for this step");
        return 1;
      }

      if (json) {
        System.out.println(JsonUtil.toJson(dataframe.schema.fields));
      } else {
        System.out.println("STEP OUTPUT SCHEMA");
        CommandLineTable st = new CommandLineTable();
        st.setShowVerticalLines(true); // if false (default) then no vertical lines are shown
        st.setHeaders(
            "NAME",
            "TYPE"); // optional - if not used then there will be no header and horizontal lines
        dataframe.schema.fields.forEach(
            v -> {
              st.addRow(v.name, v.type.toUpperCase());
            });
        st.print();
      }
    }

    if (showStepConfig) {
      if (stepId == null) {
        System.err.println("Please specify the step with --step option");
        return 1;
      }

      didSomething = true;
      if (!analyticsBoard.steps.containsKey(stepId)) {
        System.err.printf("No step with id %s found in analytics board\n", stepId);
        System.err.printf(
            "Try \"ananas show -p '%s' --steps\" to get a step list.\n", project.getAbsolutePath());
        return 1;
      }

      Step step = analyticsBoard.steps.get(stepId);
      Map<String, Object> config = step.config;
      if (json) {
        System.out.println(JsonUtil.toJson(config));
      } else {
        CommandLineTable st = new CommandLineTable();
        st.setShowVerticalLines(true); // if false (default) then no vertical lines are shown
        st.setHeaders("NAME", "VALUE");
        config.forEach(
            (name, value) -> {
              st.addRow(name, value.toString());
            });
        st.print();
      }
    }

    if (!didSomething) {
      CommandLine.usage(this, System.out);
    }

    return 0;
  }
}
