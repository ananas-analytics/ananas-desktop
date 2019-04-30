package org.ananas.runner.model.api;


import org.apache.commons.lang3.StringUtils;

public abstract class DatumaniaApiClient {

	protected String endpoint;

	protected DatumaniaApiClient() {
		//this.endpoint = String.format("http://api.%s.ananasanalytics.com/api/v1", getEnv());
		this.endpoint = "http://localhost:8080/api/v1";
	}


	private static String getEnv() {
		String env = System.getenv("DATUMANIA_ENV");
		if (StringUtils.isEmpty(env)) {
			return "prod";
		}
		return env;
	}
}
