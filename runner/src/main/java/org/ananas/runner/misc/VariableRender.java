package org.ananas.runner.misc;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class VariableRender {


	public static void render(Map<String, Object> variables, Map<String, Object> config) {
		//Replace variable occurrence in step config
		for (Entry<String, Object> kv : config.entrySet()) {
			for (Entry<String, Object> variable : variables.entrySet()) {
				if (kv.getValue() != null) {
					extract(kv, variable);
				}
			}
		}
	}

	private static void extract(Entry<String, Object> kv, Entry<String, Object> var) {
		if (kv.getValue() instanceof Boolean) {
			extractSingleBooleanValue(kv, var);
		} else if (kv.getValue() instanceof Map) {
			Map<String, Object> map = (Map<String, Object>) kv.getValue();
			for (Entry o : map.entrySet()) {
				extractSingleStringValue(o, var);
			}
		} else if (kv.getValue() instanceof List) {
			List<String> mm = (List<String>) kv.getValue();
			for (int i = 0; i < mm.size(); i++) {
				mm.set(i, replace(mm.get(i), var));
			}
		} else {
			extractSingleStringValue(kv, var);
		}
	}

	private static void extractSingleBooleanValue(Entry kv, Entry<String, Object> var) {
		boolean replacedValue = Boolean.valueOf(replace(kv.getValue(), var));
		kv.setValue(replacedValue);
	}

	private static void extractSingleStringValue(Entry kv, Entry<String, Object> var) {
		String replacedValue = replace(kv.getValue(), var);
		kv.setValue(replacedValue);
	}

	private static String replace(Object kvValue, Entry<String, Object> var) {
		return String.valueOf(kvValue).replaceAll("\\{" + var.getKey() + "\\}",
				String.valueOf(var.getValue()));
	}

}
