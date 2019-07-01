package org.ananas.runner.kernel.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.Map;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Engine implements Serializable {
  private static final long serialVersionUID = 514210961358164868L;

  public static final String APP_NAME = "app_name";

  public static final String VIEW_DB_TYPE = "database_type";
  public static final String VIEW_DB_URL = "database_url";
  public static final String VIEW_DB_USER = "database_user";
  public static final String VIEW_DB_PASSWORD = "database_password";

  public String name;
  public String type;
  public Map<String, String> properties;

  public String getProperty(String name, String defaultValue) {
    if (this.properties.containsKey(name) && this.properties.get(name) != null) {
      return this.properties.get(name);
    }
    return defaultValue;
  }

  public Double getProperty(String name, Double defaultValue) {
    if (this.properties.containsKey(name) && this.properties.get(name) != null) {
      try {
        return Double.parseDouble(this.properties.get(name));
      } catch (NumberFormatException e) {
        return defaultValue;
      }
    }
    return defaultValue;
  }

  public Integer getProperty(String name, Integer defaultValue) {
    if (this.properties.containsKey(name) && this.properties.get(name) != null) {
      try {
        return Integer.parseInt(this.properties.get(name));
      } catch (NumberFormatException e) {
        return defaultValue;
      }
    }
    return defaultValue;
  }

  public Long getProperty(String name, Long defaultValue) {
    if (this.properties.containsKey(name) && this.properties.get(name) != null) {
      try {
        return Long.parseLong(this.properties.get(name));
      } catch (NumberFormatException e) {
        return defaultValue;
      }
    }
    return defaultValue;
  }

  public Boolean getProperty(String name, Boolean defaultValue) {
    if (this.properties.containsKey(name) && this.properties.get(name) != null) {
      return new Boolean(this.properties.get(name));
    }
    return defaultValue;
  }
}
