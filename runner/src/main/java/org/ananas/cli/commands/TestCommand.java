package org.ananas.cli.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.ananas.cli.CommandLineTable;
import org.ananas.cli.DagRequestBuilder;
import org.ananas.cli.commands.extension.ExtensionHelper;
import org.ananas.runner.core.model.DagRequest;
import org.ananas.server.Services;
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

  @CommandLine.Option(
      names = {"-p", "--project"},
      description = "Ananas analytics project path, default: current directory")
  private File project = new File(".");

  @Option(
      names = {"-f", "--profile"},
      description =
          "Profile yaml file, includes execution engine, and parameters (optional). By default, local Flink engine, no parameter")
  private File profile;

  @Option(
      names = {"-m", "--param"},
      description =
          "Override the parameter defined in profile, the parameter must be defined in ananas file")
  private Map<String, String> params;

  @Option(
      names = {"-r", "--repo"},
      description = "Extension repository location, by default, ./extensions")
  private File repo = new File("./extensions");

  @Option(
      names = {"-x", "--extension"},
      description = "Extension location, could be absolute path or relative to current directory")
  private List<File> extensions;

  @Option(
      names = {"-o", "--output"},
      description = "Output file")
  private File output;

  @Option(
      names = {"-t", "--detail"},
      description = "Show test details")
  private boolean showDetails;

  @Parameters(index = "0", description = "Id of the step to test")
  private String goal;

  @Override
  public Integer call() throws Exception {
    parent.handleVerbose();

    if (ExtensionHelper.loadExtensions(repo, extensions) != 0) {
      return 1;
    }

    boolean printUsage = true;

    // create dag request placeholder object
    DagRequest dagRequest =
        DagRequestBuilder.build(project, profile, params, Collections.singletonList(goal));

    if (showDetails) {
      System.out.println("PARAMETERS");
      CommandLineTable table = new CommandLineTable();
      table.setHeaders("NAME", "TYPE", "VALUE");
      table.setShowVerticalLines(true);
      dagRequest.params.forEach(
          (k, v) -> {
            if (v != null) {
              table.addRow(k, v.type == null ? "null" : v.type, v.value == null ? "null" : v.value);
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
