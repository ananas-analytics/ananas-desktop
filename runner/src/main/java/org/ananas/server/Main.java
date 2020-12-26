package org.ananas.server;

import ch.qos.logback.classic.Level;
import org.ananas.runner.core.extension.ExtensionRegistry;
import org.ananas.runner.core.extension.LocalExtensionRepository;
import org.slf4j.LoggerFactory;

public class Main {

  public static void main(String[] args) {
    ch.qos.logback.classic.Logger rootLogger =
        (ch.qos.logback.classic.Logger)
            LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
    rootLogger.detachAppender("CLI");
    rootLogger.setLevel(Level.INFO);

    String host = "127.0.0.1";
    int port = 3003;
    if (args.length != 0) {
      host = args[0];
      if (args.length >= 2) {
        port = Integer.valueOf(args[1]);
      }
    }

    ExtensionRegistry.init();

    // init local extension repository here
    LocalExtensionRepository.getDefault().load();

    RestApiRoutes.initRestApi(host, port);
  }
}
