package org.ananas.runner.model.steps.files;

import org.ananas.runner.model.core.StepConfig;

import java.util.Map;

public class ExcelStepConfig {

	String path;
	String sheetName;

	public ExcelStepConfig(Map<String, Object> config) {
		this.sheetName = (String) config.getOrDefault(StepConfig.EXCEL_SHEET_NAME,
				null);
		this.path = (String) config.get(StepConfig.PATH);
	}
}
