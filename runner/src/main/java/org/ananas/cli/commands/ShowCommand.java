package org.ananas.cli.commands;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;
import org.ananas.cli.CommandLineTable;
import org.ananas.cli.model.AnalyticsBoard;
import org.ananas.runner.core.model.Dataframe;
import org.ananas.runner.core.model.Step;
import org.ananas.runner.misc.YamlHelper;
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
      description = "Ananas analytics project path",
      required = true)
  private File project;

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

  @Override
  public Integer call() {
    parent.handleVerbose();

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

    boolean didSomething = false;

    if (steps) {
      didSomething = true;
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

    if (variables) {
      didSomething = true;
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
      CommandLineTable st = new CommandLineTable();
      st.setShowVerticalLines(true); // if false (default) then no vertical lines are shown
      st.setHeaders("NAME", "VALUE");
      config.forEach(
          (name, value) -> {
            st.addRow(name, value.toString());
          });
      st.print();
    }

    if (!didSomething) {
      CommandLine.usage(this, System.out);
    }

    return 0;
  }
}
