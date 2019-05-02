package org.ananas.runner.model.api;


import org.apache.commons.lang3.StringUtils;

public abstract class AnanasApiClient {

	protected String endpoint;

	protected AnanasApiClient() {
		//this.endpoint = String.format("http://api.%s.ananasanalytics.com/api/v1", getEnv());
		this.endpoint = "http://localhost:8080/api/v1";
	}


	private static String getEnv() {
		String env = System.getenv("ANANAS_ENV");
		if (StringUtils.isEmpty(env)) {
			return "prod";
		}
		return env;
	}
}
