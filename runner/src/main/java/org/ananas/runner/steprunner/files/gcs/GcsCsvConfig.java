package org.ananas.runner.steprunner.files.gcs;

import java.util.Map;
import org.ananas.runner.kernel.model.StepType;
import org.ananas.runner.steprunner.files.utils.StepFileConfigToUrl;
import org.apache.commons.csv.CSVFormat;

public class GcsCsvConfig {
  public String url;
  public boolean hasHeader;
  public char delimiter;
  public String recordSeparator;

  public GcsCsvConfig(StepType type, Map<String, Object> config) {
    this.recordSeparator =
        config.get("recordSeparator") == null
            ? CSVFormat.DEFAULT.getRecordSeparator()
            : (String) config.get("recordSeparator");
    this.delimiter =
        config.get("delimiter") == null
            ? CSVFormat.DEFAULT.getDelimiter()
            : (char) config.get("delimiter");
    this.url = StepFileConfigToUrl.gcsSourceUrl(config);
    this.hasHeader = (Boolean) config.getOrDefault("header", false);
  }
}
