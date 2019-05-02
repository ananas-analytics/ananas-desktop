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
	static public class Env {
		String name;
		String type;
	}

	public Map<String, Object> params;
	public Set<String> goals;
	public Env env;


	public Dag dag;

	public DagRequest() {
		this.params = new HashMap<>();
		this.goals = new HashSet<>();
	}


}
