package org.ananas.runner.legacy.steps.files;

import java.util.Map;
import org.ananas.runner.legacy.core.StepConfig;

public class ExcelStepConfig {

  String path;
  String sheetName;

  public ExcelStepConfig(Map<String, Object> config) {
    this.sheetName = (String) config.getOrDefault(StepConfig.EXCEL_SHEET_NAME, null);
    this.path = (String) config.get(StepConfig.PATH);
  }
}
