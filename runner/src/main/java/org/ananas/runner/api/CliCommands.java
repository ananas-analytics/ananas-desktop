package org.ananas.runner.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.ananas.runner.legacy.core.Project;
import org.apache.commons.cli.*;
import spark.utils.IOUtils;

public class CliCommands {

  protected static void initCommandLine(String[] args) {
    final CommandLineParser parser = new PosixParser();
    final Options options = new Options();

    Option action =
        new Option(
            "test",
            false,
            "Test a project step. This will test a step without recording its state. For testing purpose input dataframe are sliced and sampled.");
    options.addOption(action);
    Option optionDir = new Option("d", "project_directory", true, "use given directory path");
    optionDir.setRequired(true);
    options.addOption(optionDir);
    Option optionProfiles =
        new Option(
            "p",
            "profile_file",
            true,
            "use given profile file. Variables are defined in this file.");
    options.addOption(optionProfiles);
    Option optionStep = new Option("i", "step_id", true, "The id of the step");
    optionStep.setRequired(true);
    options.addOption(optionStep);

    // automatically generate the help statement

    CommandLine cmd = null;
    try {
      cmd = parser.parse(options, args);
    } catch (ParseException e) {
      System.out.println(e.getMessage());
      return;
    }
    if (cmd.hasOption("test")) {
      testCommand(cmd);
      return;
    } else {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("test", options);
    }
  }

  private static void testCommand(CommandLine cmd) {
    String path = cmd.getOptionValue("d");
    String stepId = cmd.getOptionValue("i");
    String profileFilePath = cmd.getOptionValue("p");
    try {
      Project dag = openYAML(path + "/ananas.yml", Project.class);
      Map variables = new HashMap();
      if (profileFilePath != null) {
        variables = openYAML(profileFilePath, Map.class);
      }
      System.out.println(Services.testDag(dag.toDagRequest(stepId, variables)));
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  private static <T> T openYAML(String path, Class<T> clazz) throws IOException {
    FileInputStream in = new FileInputStream(path);
    String yaml = IOUtils.toString(in);
    ObjectMapper yamlReader =
        new ObjectMapper(new com.fasterxml.jackson.dataformat.yaml.YAMLFactory());
    return yamlReader.readValue(yaml, clazz);
  }
}
