package org.ananas.cli.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import org.ananas.cli.CommandLineTable;
import org.ananas.cli.DagRequestBuilder;
import org.ananas.cli.model.AnalyticsBoard;
import org.ananas.cli.model.Profile;
import org.ananas.runner.api.Services;
import org.ananas.runner.kernel.model.Dag;
import org.ananas.runner.kernel.model.DagRequest;
import org.ananas.runner.kernel.model.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import spark.utils.IOUtils;

@Command(name = "test", description = "Test the settings of one step")
public class TestCommand implements Callable {
  private static final Logger LOG = LoggerFactory.getLogger(TestCommand.class);

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
      names = {"-o", "--output"},
      description = "Output file")
  private File output;

  @Option(
    names = {"-t", "--detail"},
    description = "Show test details"
  )
  private boolean showDetails;

  @Parameters(index = "0", description = "Id of the step to test")
  private String goal;

  @Override
  public Integer call() throws Exception {
    parent.handleVerbose();

    boolean printUsage = true;

    // create dag request placeholder object
    DagRequest dagRequest = DagRequestBuilder.build(project, profile, params, Collections.singletonList(goal));

    if (showDetails) {
      System.out.println("PARAMETERS");
      CommandLineTable table = new CommandLineTable();
      table.setHeaders("NAME", "TYPE", "VALUE");
      table.setShowVerticalLines(true);
      dagRequest.params.forEach((k, v) -> {
        if (v != null) {
          table.addRow(k,
            v.type == null ? "null" : v.type,
            v.value == null ? "null" : v.value);
        }
      });
      table.print();
    }

    DagRequest resolvedDagRequest = dagRequest.resolveVariables();
    printUsage = false;
    try {
      Object result = Services.testDag(resolvedDagRequest);
      if (output == null) {
        System.out.println(result);
      } else {
        FileOutputStream out = new FileOutputStream(output);
        if (!output.exists()) {
          output.createNewFile();
        }
        out.write(result.toString().getBytes());
        out.flush();
        out.close();
      }
    } catch (Exception e) {
      System.err.println("Failed to test step");
      e.printStackTrace();
      return 1;
    }

    LOG.info("Test done! Your step settings look good!");

    if (printUsage) {
      CommandLine.usage(this, System.out);
      return 1;
    }

    return 0;
  }

  private static <T> T openYAML(String path, Class<T> clazz) throws IOException {
    FileInputStream in = new FileInputStream(path);
    String yaml = IOUtils.toString(in);
    ObjectMapper yamlReader =
        new ObjectMapper(new com.fasterxml.jackson.dataformat.yaml.YAMLFactory());
    return yamlReader.readValue(yaml, clazz);
  }
}
