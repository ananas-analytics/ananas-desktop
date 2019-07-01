package org.ananas.runner.kernel.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import freemarker.core.RTFOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import lombok.Data;
import org.ananas.runner.kernel.common.JsonUtil;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DagRequest {

  public static Configuration TEMPLATE_CFG = new Configuration(Configuration.VERSION_2_3_27);

  static {
    TEMPLATE_CFG.setDefaultEncoding("UTF-8");
    TEMPLATE_CFG.setLogTemplateExceptions(false);
    TEMPLATE_CFG.setWrapUncheckedExceptions(true);
    TEMPLATE_CFG.setOutputFormat(
        RTFOutputFormat.INSTANCE); // useful to escape the injected /, {, and }
  }

  public Dag dag;
  public Set<String> goals;
  public Engine engine;
  public Map<String, Variable> params;
  public TriggerOptions trigger;

  public DagRequest() {
    this.params = new HashMap<>();
    this.goals = new HashSet<>();
    this.trigger = TriggerOptionsFactory.runOnceNow(); // by default trigger once immediately
  }

  /**
   * Replace the variable references with their values
   *
   * @return a new DagRequest object, with variables replaced by their values
   * @throws IOException
   * @throws TemplateException
   */
  public DagRequest resolveVariables() throws IOException, TemplateException {
    String rawReq = JsonUtil.toJson(this);

    Template t = new Template("DagRequest", new StringReader(rawReq), TEMPLATE_CFG);

    // build legacy
    Map model = new HashMap();
    for (String key : this.params.keySet()) {
      Variable v = this.params.get(key);
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
    t.process(model, out);

    String transformedTemplate = out.toString();
    return JsonUtil.fromJson(transformedTemplate, DagRequest.class);
  }
}
