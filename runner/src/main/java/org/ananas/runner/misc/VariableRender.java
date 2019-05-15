package org.ananas.runner.misc;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.ananas.runner.api.JsonUtil;
import org.ananas.runner.model.core.DagRequest;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class VariableRender {

	public static Map<String, Object> renderConfig(Map<String, DagRequest.Variable> variables, Map<String, Object> config)  {
		String json = JsonUtil.toJson(config);
		Template t = null;
		try {
			t = new Template("StepConfig", new StringReader(json), DagRequest.TEMPLATE_CFG);
		} catch (IOException e) {
			e.printStackTrace();
			return config;
		}

		// build model
		Map model = new HashMap();
		for (String key : variables.keySet()) {
			DagRequest.Variable v = variables.get(key);
			switch(v.type) {
				case "number":
					model.put(key, v.convertToNumber());
					break;
				case "date":
					model.put(key, v.convertToDate());
					break;
				default:
					model.put(key, v.value);
			}
		}

		Writer out = new StringWriter();
		try {
			t.process(model, out);
			String transformedTemplate = out.toString();
			return JsonUtil.fromJson(transformedTemplate, Map.class);
		} catch (IOException | TemplateException e) {
			e.printStackTrace();
		}

		return config;
	}

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
