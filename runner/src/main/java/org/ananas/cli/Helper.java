package org.ananas.cli;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import org.ananas.cli.model.AnalyticsBoard;
import org.ananas.runner.misc.YamlHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Helper {

  private static final Logger LOG = LoggerFactory.getLogger(Helper.class);

  public static AnalyticsBoard createAnalyticsBoard(File project) {
    AnalyticsBoard analyticsBoard = null;
    File ananas = Paths.get(project.getAbsolutePath(), "ananas.yml").toFile();
    if (!ananas.exists()) {
      ananas = Paths.get(project.getAbsolutePath(), "ananas.yaml").toFile();
      if (!ananas.exists()) {
        System.err.println("Can't find ananas.yml file in your project");
        return null;
      }
    }
    try {
      analyticsBoard = YamlHelper.openYAML(ananas.getAbsolutePath(), AnalyticsBoard.class);
    } catch (Exception e) {
      System.err.println("Failed to parse analytics board file: " + e.getLocalizedMessage());
      Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).forEach(LOG::error);
      return null;
    }

    return analyticsBoard;
  }
}
