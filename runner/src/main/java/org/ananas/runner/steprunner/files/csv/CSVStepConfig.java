package org.ananas.runner.steprunner.files.csv;

import java.util.Map;
import org.ananas.runner.kernel.model.StepType;
import org.ananas.runner.model.steps.files.FileLoader;
import org.ananas.runner.steprunner.files.utils.StepFileConfigToUrl;
import org.apache.commons.csv.CSVFormat;

public class CSVStepConfig {
  public String url;
  public boolean hasHeader;
  public char delimiter;
  public String recordSeparator;

  public CSVStepConfig(StepType type, Map<String, Object> config) {
    this.recordSeparator =
        config.get("recordSeparator") == null
            ? CSVFormat.DEFAULT.getRecordSeparator()
            : (String) config.get("recordSeparator");
    this.delimiter =
        config.get("delimiter") == null
            ? CSVFormat.DEFAULT.getDelimiter()
            : (char) config.get("delimiter");
    this.url = StepFileConfigToUrl.url(type, config, FileLoader.SupportedFormat.CSV);
    this.hasHeader = (Boolean) config.getOrDefault("header", false);
  }
}
