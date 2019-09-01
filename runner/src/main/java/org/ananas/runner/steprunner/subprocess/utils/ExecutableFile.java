package org.ananas.runner.steprunner.subprocess.utils;

import org.ananas.runner.steprunner.subprocess.SubProcessConfiguration;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Contains the configuration for the external library. */
@DefaultCoder(AvroCoder.class)
public class ExecutableFile {

  String fileName;

  private String sourceGCSLocation;
  private String destinationLocation;

  static final Logger LOG = LoggerFactory.getLogger(ExecutableFile.class);

  public String getSourceGCSLocation() {
    return sourceGCSLocation;
  }

  public void setSourceGCSLocation(String sourceGCSLocation) {
    this.sourceGCSLocation = sourceGCSLocation;
  }

  public String getDestinationLocation() {
    return destinationLocation;
  }

  public void setDestinationLocation(String destinationLocation) {
    this.destinationLocation = destinationLocation;
  }

  public ExecutableFile(SubProcessConfiguration configuration, String fileName)
      throws IllegalStateException {
    if (configuration == null) {
      throw new IllegalStateException("Configuration can not be NULL");
    }
    if (fileName == null) {
      throw new IllegalStateException("FileName can not be NULLt");
    }
    this.fileName = fileName;
    setDestinationLocation(configuration);
    setSourceLocation(configuration);
  }

  private void setDestinationLocation(SubProcessConfiguration configuration) {
    this.sourceGCSLocation =
        FileUtils.getFileResourceId(configuration.getSourcePath(), fileName).toString();
  }

  private void setSourceLocation(SubProcessConfiguration configuration) {
    this.destinationLocation =
        FileUtils.getFileResourceId(configuration.getWorkerPath(), fileName).toString();
  }
}
