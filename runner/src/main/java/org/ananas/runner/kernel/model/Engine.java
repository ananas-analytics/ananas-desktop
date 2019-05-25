package org.ananas.runner.kernel.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Engine {

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
