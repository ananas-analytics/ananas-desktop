package org.ananas.acceptance.helper;

import java.util.Properties;
import org.ananas.TestHelper;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.SystemOutRule;

public class AcceptanceTestBase {
  @Rule public final ExpectedSystemExit exit = ExpectedSystemExit.none();

  @Rule public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

  @Rule
  public final ProvideSystemProperty filesToStage =
      new ProvideSystemProperty("filesToStage", TestHelper.getCLIPath());

  @Rule
  public final ProvideSystemProperty threadFactory =
      new ProvideSystemProperty(
          "java.util.concurrent.ForkJoinPool.common.threadFactory",
          AcceptanceForkJoinThreadFactory.class.getName());

  public static String testPropertiesFile = TestHelper.getTestPropertyFile();

  public static Properties props = TestHelper.loadProperties(testPropertiesFile);
}
