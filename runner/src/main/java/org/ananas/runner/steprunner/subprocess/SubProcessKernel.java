package org.ananas.runner.steprunner.subprocess;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.ananas.runner.steprunner.subprocess.utils.CallingSubProcessUtils;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.SeekableByteArrayInput;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the process kernel which deals with exec of the subprocess. It also deals with all I/O.
 */
public class SubProcessKernel {

  private static final Logger LOG = LoggerFactory.getLogger(SubProcessKernel.class);

  private static final int MAX_SIZE_COMMAND_LINE_ARGS = 128 * 1024;

  SubProcessConfiguration configuration;
  ProcessBuilder processBuilder;
  Schema outputSchema;
  Schema inputSchema;

  private SubProcessKernel() {}

  /**
   * Creates the SubProcess Kernel ready for execution. Will deal with all input and outputs to the
   * SubProcess
   *
   * @param options
   */
  public SubProcessKernel(SubProcessConfiguration options, String outputSchema) {
    this.configuration = options;
    this.processBuilder =
        new ProcessBuilder(
            configuration.binaryName, configuration.workerPath + configuration.executableName);
    this.outputSchema = new Schema.Parser().parse(outputSchema);
  }

  public List<GenericRecord> exec(byte[] b) throws Exception {
    try (CallingSubProcessUtils.Permit permit =
        new CallingSubProcessUtils.Permit(configuration.executableName)) {

      List<GenericRecord> results = new ArrayList<>();
      try {
        Process process = execBinary(processBuilder, b);
        results = collectProcessResults(process, processBuilder);
      } catch (Exception ex) {
        LOG.error("Error running executable ", ex);
        throw ex;
      }
      return results;
    }
  }

  /**
   * Add up the total bytes used by the process.
   *
   * @param commands
   * @return
   */
  private int getTotalCommandBytes(SubProcessCommandLineArgs commands) {
    int size = 0;
    for (SubProcessCommandLineArgs.Command c : commands.getParameters()) {
      size += c.value.length();
    }
    return size;
  }

  private Process execBinary(ProcessBuilder builder, byte[] arg) throws Exception {
    try {
      builder.command().add(Base64.getEncoder().encodeToString(arg));

      builder.inheritIO().redirectInput(ProcessBuilder.Redirect.PIPE);
      builder.inheritIO().redirectError(ProcessBuilder.Redirect.PIPE);
      builder.inheritIO().redirectOutput(ProcessBuilder.Redirect.PIPE);

      Process process = builder.start();

      boolean timeout = !process.waitFor(configuration.getWaitTime(), TimeUnit.SECONDS);

      if (timeout) {
        String log =
            String.format(
                "Timeout waiting to run process with parameters %s . "
                    + "Check to see if your timeout is long enough. Currently set at %s.",
                createLogEntryFromInputs(builder.command()), configuration.getWaitTime());
        throw new Exception(log);
      }
      return process;

    } catch (Exception ex) {

      LOG.error(
          String.format(
              "Error running process with parameters %s error was %s ",
              createLogEntryFromInputs(builder.command()), ex.getMessage()));
      throw new Exception(ex);
    }
  }

  /**
   * TODO clean up duplicate with byte[] version collectBinaryProcessResults.
   *
   * @param process
   * @param builder
   * @return List of results
   * @throws Exception if process has non 0 value or no logs found then throw exception
   */
  private List<GenericRecord> collectProcessResults(Process process, ProcessBuilder builder)
      throws Exception {

    List<GenericRecord> results = new ArrayList<>();

    try {

      // LOG.debug(String.format("Executing process %s",
      // createLogEntryFromInputs(builder.command())));

      // If process exit value is not 0 then subprocess failed, record logs
      if (process.exitValue() != 0) {
        // TODO outPutFiles.copyOutPutFilesToBucket(configuration,
        // FileUtils.toStringParams(builder));
        // TODO String log = createLogEntryForProcessFailure(process, builder.command(),
        // outPutFiles);
        throw new Exception("exit code is " + process.exitValue());
      }

      // If no return file then either something went wrong or the binary is setup incorrectly for
      // the ret file either way throw error
      /*if (!Files.exists(outPutFiles.resultFile)) {
        String log = createLogEntryForProcessFailure(process, builder.command(), outPutFiles);
        outPutFiles.copyOutPutFilesToBucket(configuration, FileUtils.toStringParams(builder));
        throw new Exception(log);
      }*/

      // Everything looks healthy return values
      GenericDatumReader<GenericRecord> datumReader = new GenericDatumReader<>(outputSchema);
      try (DataFileReader<GenericRecord> r =
          new DataFileReader<GenericRecord>(
              new SeekableByteArrayInput(IOUtils.toByteArray(process.getInputStream())),
              datumReader)) {
        while (r.hasNext()) results.add(r.next());
      } catch (Exception e) {
        e.printStackTrace();
      }

      /*try (BufferedReader reader =
             new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
        String line = reader.readLine();
        if (Strings.isNullOrEmpty(line)) {
          throw new RuntimeException(line);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }*/

      return results;
    } catch (Exception ex) {
      String log =
          String.format(
              "Unexpected error running process. %s error message was %s",
              createLogEntryFromInputs(builder.command()), ex.getMessage());
      System.err.println(log);
      throw new Exception(log);
    }
  }

  /*private static String createLogEntryForProcessFailure(Process process, List<String> commands) {

    StringBuilder stringBuilder = new StringBuilder();

    // Highlight when no result file is found vs standard process error
    if (process.exitValue() == 0) {
      stringBuilder.append(String.format("%nProcess succeded but no result file was found %n"));
    } else {
      stringBuilder.append(
          String.format("%nProcess error failed with exit value of %s %n", process.exitValue()));
    }

    stringBuilder.append(
        String.format("Command info was %s %n", createLogEntryFromInputs(commands)));

    stringBuilder.append(
        String.format(
            "First line of error file is  %s %n", FileUtils.readLineOfLogFile(files.errFile)));

    stringBuilder.append(
        String.format(
            "First line of out file is %s %n", FileUtils.readLineOfLogFile(files.outFile)));

    stringBuilder.append(
        String.format(
            "First line of ret file is %s %n", FileUtils.readLineOfLogFile(files.resultFile)));

    return stringBuilder.toString();
  }*/

  private static String createLogEntryFromInputs(List<String> commands) {
    String params;
    if (commands != null) {
      params = String.join(",", commands);
    } else {
      params = "No-Commands";
    }
    return params;
  }
}
