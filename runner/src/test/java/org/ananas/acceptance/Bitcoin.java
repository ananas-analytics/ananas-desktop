package org.ananas.acceptance;

import com.jayway.jsonpath.JsonPath;
import org.ananas.acceptance.helper.AcceptanceForkJoinThreadFactory;
import org.ananas.cli.Main;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.Assertion;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.SystemOutRule;

import java.net.URL;
import java.util.List;
import java.util.Map;

public class Bitcoin {
  @Rule
  public final ExpectedSystemExit exit = ExpectedSystemExit.none();

  @Rule
  public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

  @Rule
  public final ProvideSystemProperty filesToStage
    = new ProvideSystemProperty("filesToStage", "mock.jar");

  @Rule
  public final ProvideSystemProperty threadFactory
    = new ProvideSystemProperty("java.util.concurrent.ForkJoinPool.common.threadFactory", AcceptanceForkJoinThreadFactory.class.getName());

  @Test
  public void exploreAPISource() {
    exit.expectSystemExitWithStatus(0);

    exit.checkAssertionAfterwards(new Assertion() {
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

    Main.main(new String[]{
      "explore",
      "-p",
      project.getPath(),
      "5d409da52c03930c36c3fdb9",
      "-n",
      "0",
      "--size",
      "5"
    });
  }


}
