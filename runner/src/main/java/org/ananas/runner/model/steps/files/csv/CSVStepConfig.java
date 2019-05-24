package org.ananas.runner.model.steps.files.csv;

import java.util.Map;
import org.ananas.runner.model.core.StepConfig;
import org.ananas.runner.model.steps.commons.StepType;
import org.ananas.runner.model.steps.files.FileLoader;
import org.ananas.runner.model.steps.files.utils.StepFileConfigToUrl;
import org.apache.commons.csv.CSVFormat;

public class CSVStepConfig {
  String url;
  boolean hasHeader;
  char delimiter;
  String recordSeparator;

  public CSVStepConfig(StepType type, Map<String, Object> config) {
    this.recordSeparator =
        config.get(StepConfig.recordSeparator) == null
            ? CSVFormat.DEFAULT.getRecordSeparator()
            : (String) config.get(StepConfig.recordSeparator);
    this.delimiter =
        config.get(StepConfig.DELIMITER) == null
            ? CSVFormat.DEFAULT.getDelimiter()
            : (char) config.get(StepConfig.DELIMITER);
    this.url = StepFileConfigToUrl.url(type, config, FileLoader.SupportedFormat.CSV);
    this.hasHeader = (Boolean) config.getOrDefault(StepConfig.IS_HEADER, false);
  }
}
