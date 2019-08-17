package org.ananas.acceptance;

import com.jayway.jsonpath.JsonPath;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.ananas.TestHelper;
import org.ananas.acceptance.helper.AcceptanceForkJoinThreadFactory;
import org.ananas.cli.Main;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.Assertion;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.SystemOutRule;

public class MySQLSource {
  @Rule public final ExpectedSystemExit exit = ExpectedSystemExit.none();

  @Rule public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

  @Rule
  public final ProvideSystemProperty filesToStage =
      new ProvideSystemProperty("filesToStage", "mock.jar");

  @Rule
  public final ProvideSystemProperty threadFactory =
      new ProvideSystemProperty(
          "java.util.concurrent.ForkJoinPool.common.threadFactory",
          AcceptanceForkJoinThreadFactory.class.getName());

  private static String testPropertiesFile = TestHelper.getTestPropertyFile();

  private static Properties props = TestHelper.loadProperties(testPropertiesFile);

  @Test
  public void exploreMySQLSource() {
    exit.expectSystemExitWithStatus(0);

    URL project = TestHelper.getResource("test_projects/mysql");

    exit.checkAssertionAfterwards(
        new Assertion() {
          public void checkAssertion() {
            String json = systemOutRule.getLog();
            int code = JsonPath.read(json, "$.code");
            Assert.assertEquals(200, code);

            List<Map<String, String>> fields = JsonPath.read(json, "$.data.schema.fields");
            Assert.assertTrue(fields.size() > 0);
          }
        });

    Main.main(
        new String[] {
          "explore",
          "-p",
          project.getPath(),
          "5d56c902d4dbe821c135eb94",
          "-n",
          "0",
          "--size",
          "5",
          "-m",
          "PASSWORD=" + props.getProperty("mysql.password"),
          "-m",
          "DB=classicmodels",
          "-m",
          "HOST=" + props.getProperty("mysql.host"),
        });
  }
}
