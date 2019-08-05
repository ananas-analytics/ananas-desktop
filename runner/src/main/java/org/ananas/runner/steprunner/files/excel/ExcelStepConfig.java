package org.ananas.runner.steprunner.files.excel;

import java.util.Map;
import org.ananas.runner.legacy.core.StepConfig;

public class ExcelStepConfig {

  // EXCEL_SHEET_NAME
  static final String EXCEL_SHEET_NAME = "sheetname";

  String path;
  String sheetName;

  public ExcelStepConfig(Map<String, Object> config) {
    this.sheetName = (String) config.getOrDefault(EXCEL_SHEET_NAME, null);
    this.path = (String) config.get(StepConfig.PATH);
  }
}
