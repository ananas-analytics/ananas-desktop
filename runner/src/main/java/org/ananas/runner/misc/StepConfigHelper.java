package org.ananas.runner.misc;

import java.util.Map;

public class StepConfigHelper {
  public static String getConfig(Map<String, Object> config, String name, String defaultValue) {
    if (config.containsKey(name) && config.get(name) != null) {
      Object v = config.get(name);
      if (v instanceof String) {
        return (String) v;
      }
      return v.toString();
    }
    return defaultValue;
  }

  public static Boolean getConfig(Map<String, Object> config, String name, Boolean defaultValue) {
    if (config.containsKey(name) && config.get(name) != null) {
      Object v = config.get(name);
      if (v instanceof String) {
        return Boolean.valueOf((String) v);
      }
      if (v instanceof Boolean) {
        return (Boolean) v;
      }
    }
    return defaultValue;
  }

  public static Integer getConfig(Map<String, Object> config, String name, Integer defaultValue) {
    if (config.containsKey(name) && config.get(name) != null) {
      Object v = config.get(name);
      if (v instanceof String) {
        try {
          return Integer.parseInt((String) v);
        } catch (Exception e) {
          return defaultValue;
        }
      }
      if (v instanceof Integer) {
        return (Integer) v;
      }
    }
    return defaultValue;
  }

  public static Double getConfig(Map<String, Object> config, String name, Double defaultValue) {
    if (config.containsKey(name) && config.get(name) != null) {
      Object v = config.get(name);
      if (v instanceof String) {
        try {
          return Double.parseDouble((String) v);
        } catch (Exception e) {
          return defaultValue;
        }
      }
      if (v instanceof Integer) {
        return new Double((Integer) v);
      }
      if (v instanceof Double) {
        return (Double) v;
      }
    }
    return defaultValue;
  }

  public static Float getConfig(Map<String, Object> config, String name, Float defaultValue) {
    Double d = getConfig(config, name, new Double(defaultValue));
    return new Float(d);
  }
}
