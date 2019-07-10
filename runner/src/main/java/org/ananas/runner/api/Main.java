package org.ananas.runner.api;

import ch.qos.logback.classic.Level;
import org.ananas.runner.kernel.ExtensionRegistry;
import org.slf4j.LoggerFactory;

public class Main {

  public static void main(String[] args) {
    ch.qos.logback.classic.Logger rootLogger =
        (ch.qos.logback.classic.Logger)
            LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
    rootLogger.detachAppender("CLI");
    rootLogger.setLevel(Level.INFO);

    ExtensionRegistry.init();

    RestApiRoutes.initRestApi(args);
  }
}
