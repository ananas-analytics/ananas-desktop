package org.ananas.runner.core.common;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import org.ananas.runner.core.model.DagRequest;
import org.ananas.runner.core.model.Variable;

public class VariableRender {

  public static Map<String, Object> renderConfig(
      Map<String, Variable> variables, Map<String, Object> config) {
    String json = JsonUtil.toJson(config);
    Template t = null;
    try {
      t = new Template("StepConfig", new StringReader(json), DagRequest.TEMPLATE_CFG);
    } catch (IOException e) {
      // e.printStackTrace();
      throw new RuntimeException(e);
      // return config;
    }

    // build legacy
    Map model = new HashMap();
    for (String key : variables.keySet()) {
      Variable v = variables.get(key);
      switch (v.type) {
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
      throw new RuntimeException(e);
    }
  }
}
