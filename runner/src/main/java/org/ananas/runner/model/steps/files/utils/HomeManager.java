package org.ananas.runner.model.steps.files.utils;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HomeManager {

  private static final Logger LOG = LoggerFactory.getLogger(HomeManager.class);

  private static final String PREFIX = "ananas";

  public static String getHome() {
    return System.getProperty("user.home") + File.separator + PREFIX;
  }

  public static String getHomeFilePath(String fileName) {
    return getHome() + File.separator + fileName;
  }

  public static String getTempDirectory() {
    return System.getProperty("java.io.tmpdir");
  }

  public static void main(String[] args) {
    LOG.debug("Home {}", getHome());
  }
}
