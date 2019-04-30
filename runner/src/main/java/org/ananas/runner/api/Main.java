package org.ananas.runner.api;


public class Main {


	public static void main(String[] args) {
		if (args.length == 0) {
			RestApiRoutes.initRestApi(args);
		} else {

			CliCommands.initCommandLine(args);
		}
	}


}
