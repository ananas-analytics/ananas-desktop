package org.ananas.runner.model.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaginationBody {
	public String type;
	public Map<String, DagRequest.Variable> params;
	public Map<String, Object> config;
}
