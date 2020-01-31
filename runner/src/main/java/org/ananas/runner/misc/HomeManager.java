package org.ananas.runner.misc;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HomeManager {

  private static final Logger LOG = LoggerFactory.getLogger(HomeManager.class);

  private static final String PREFIX = "ananas";

  private static final String EXTENSION_FOLDER = "extensions";

  public static String getHome() {
    return System.getProperty("user.home") + File.separator + PREFIX;
  }

  public static String getHomeFilePath(String fileName) {
    return getHome() + File.separator + fileName;
  }

  public static String getHomeExtensionPath() {
    return getHomeFilePath(EXTENSION_FOLDER);
  }
}
