package org.ananas.runner.model.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DagRequest {

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	static public class Engine {
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

	public Map<String, Object> params;
	public Set<String> goals;
	public Engine engine;

	public Dag dag;

	public DagRequest() {
		this.params = new HashMap<>();
		this.goals = new HashSet<>();
	}
}
