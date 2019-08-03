package org.ananas.acceptance;

import com.jayway.jsonpath.JsonPath;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.ananas.acceptance.helper.AcceptanceForkJoinThreadFactory;
import org.ananas.cli.Main;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.Assertion;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.SystemOutRule;

public class Bitcoin {
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

  @Test
  public void exploreAPISource() {
    exit.expectSystemExitWithStatus(0);

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

    ClassLoader classLoader = getClass().getClassLoader();
    URL project = classLoader.getResource("test_projects/bitcoin");

    Main.main(
        new String[] {
          "explore", "-p", project.getPath(), "5d409da52c03930c36c3fdb9", "-n", "0", "--size", "5"
        });
  }

  @Test
  public void queryAPISourceWithSQLTransform() {
    exit.expectSystemExitWithStatus(0);

    exit.checkAssertionAfterwards(
        new Assertion() {
          public void checkAssertion() {
            String json = systemOutRule.getLog();
            int code = JsonPath.read(json, "$.code");
            Assert.assertEquals(200, code);

            List<Map<String, String>> fields =
                JsonPath.read(json, "$.data.5d4557439c7a5441fdc67d3b.schema.fields");
            Assert.assertTrue(fields.size() > 0);

            Assert.assertEquals("code", fields.get(0).get("name"));
            Assert.assertEquals("VARCHAR", fields.get(0).get("type"));

            Assert.assertEquals("rate", fields.get(1).get("name"));
            Assert.assertEquals("DECIMAL", fields.get(1).get("type"));

            Assert.assertEquals("time", fields.get(2).get("name"));
            Assert.assertEquals("VARCHAR", fields.get(2).get("type"));
          }
        });

    ClassLoader classLoader = getClass().getClassLoader();
    URL project = classLoader.getResource("test_projects/bitcoin");

    Main.main(
        new String[] {
          "test", "-p", project.getPath(), "5d4557439c7a5441fdc67d3b",
        });
  }

  @Test
  public void testRun() {
    exit.expectSystemExitWithStatus(0);

    exit.checkAssertionAfterwards(
        new Assertion() {
          public void checkAssertion() {
            String json = systemOutRule.getLog();
            int code = JsonPath.read(json, "$.code");
            Assert.assertEquals(200, code);

            String jobId = JsonPath.read(json, "$.data.jobid");
            Assert.assertNotNull(jobId);
          }
        });

    ClassLoader classLoader = getClass().getClassLoader();
    URL project = classLoader.getResource("test_projects/bitcoin");

    Main.main(
        new String[] {
          "run", "-p", project.getPath(), "5d4559249c7a5441fdc67d47",
        });
  }
}
