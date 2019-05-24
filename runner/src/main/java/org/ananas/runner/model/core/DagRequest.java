package org.ananas.runner.model.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import freemarker.core.RTFOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import lombok.Data;
import org.ananas.runner.api.JsonUtil;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DagRequest {

  public static Configuration TEMPLATE_CFG = new Configuration(Configuration.VERSION_2_3_27);

  static {
    TEMPLATE_CFG.setDefaultEncoding("UTF-8");
    TEMPLATE_CFG.setLogTemplateExceptions(false);
    TEMPLATE_CFG.setWrapUncheckedExceptions(true);
    // useful to escape the injected /, {, and }
    TEMPLATE_CFG.setOutputFormat(RTFOutputFormat.INSTANCE);
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Engine {

    public String name;
    public String type;
    public Map<String, String> properties;

    public String getProperty(String name, String defaultValue) {
      if (this.properties.containsKey(name)) {
        return this.properties.get(name);
      }
      return defaultValue;
    }

    public Double getProperty(String name, Double defaultValue) {
      if (this.properties.containsKey(name)) {
        try {
          return Double.parseDouble(this.properties.get(name));
        } catch (NumberFormatException e) {
          return defaultValue;
        }
      }
      return defaultValue;
    }

    public Integer getProperty(String name, Integer defaultValue) {
      if (this.properties.containsKey(name)) {
        try {
          return Integer.parseInt(this.properties.get(name));
        } catch (NumberFormatException e) {
          return defaultValue;
        }
      }
      return defaultValue;
    }

    public Long getProperty(String name, Long defaultValue) {
      if (this.properties.containsKey(name)) {
        try {
          return Long.parseLong(this.properties.get(name));
        } catch (NumberFormatException e) {
          return defaultValue;
        }
      }
      return defaultValue;
    }

    public Boolean getProperty(String name, Boolean defaultValue) {
      if (this.properties.containsKey(name)) {
        return new Boolean(this.properties.get(name));
      }
      return defaultValue;
    }
  }

  public static class Variable {

    public String name;
    public String type;
    public String description;
    public String scope;
    public String value;

    public Date convertToDate() {
      // ISO8601
      DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_DATE_TIME;
      OffsetDateTime offsetDateTime = OffsetDateTime.parse(this.value, timeFormatter);
      Date date = Date.from(Instant.from(offsetDateTime));
      return date;
    }

    public Double convertToNumber() {
      return Double.parseDouble(this.value);
    }
  }

  public Map<String, Variable> params;
  public Set<String> goals;
  public Engine engine;
  public Dag dag;

  public DagRequest() {
    this.params = new HashMap<>();
    this.goals = new HashSet<>();
  }

  public DagRequest resolveVariables() throws IOException, TemplateException {
    String rawReq = JsonUtil.toJson(this);

    Template t = new Template("DagRequest", new StringReader(rawReq), TEMPLATE_CFG);

    // build model
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
