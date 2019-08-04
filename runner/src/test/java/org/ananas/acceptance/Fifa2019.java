package org.ananas.acceptance;

import com.jayway.jsonpath.JsonPath;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.ananas.acceptance.helper.AcceptanceForkJoinThreadFactory;
import org.ananas.acceptance.helper.DataViewerHelper;
import org.ananas.cli.Main;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.*;

public class Fifa2019 {
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
  public void exploreDataSource() {
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
    URL project = classLoader.getResource("test_projects/Fifa2019");

    Main.main(
        new String[] {
          "explore", "-p", project.getPath(), "5d31c71a84b5674b6a220288", "-n", "0", "--size", "5"
        });
  }

  @Test
  public void testTransformer() {
    exit.expectSystemExitWithStatus(0);

    exit.checkAssertionAfterwards(
        new Assertion() {
          public void checkAssertion() {
            String json = systemOutRule.getLog();
            int code = JsonPath.read(json, "$.code");
            Assert.assertEquals(200, code);

            List<Map<String, String>> fields =
                JsonPath.read(json, "$.data.5d31cb6684b5674b6a22028c.schema.fields");
            Assert.assertTrue(fields.size() > 0);

            List<Object> data = JsonPath.read(json, "$.data.5d31cb6684b5674b6a22028c.data");
            Assert.assertTrue(data.size() > 0);
          }
        });

    // run command line with arguments
    ClassLoader classLoader = getClass().getClassLoader();
    URL project = classLoader.getResource("test_projects/Fifa2019");

    Main.main(
        new String[] {
          "test", "-p", project.getPath(), "5d31cb6684b5674b6a22028c",
        });
  }

  @Test
  public void testConcatStep() {
    exit.expectSystemExitWithStatus(0);

    exit.checkAssertionAfterwards(
        new Assertion() {
          public void checkAssertion() {
            String json = systemOutRule.getLog();
            int code = JsonPath.read(json, "$.code");
            Assert.assertEquals(200, code);

            List<Map<String, String>> fields =
                JsonPath.read(json, "$.data.5d31d45184b5674b6a2202c2.schema.fields");
            Assert.assertTrue(fields.size() > 0);

            List<Object> data = JsonPath.read(json, "$.data.5d31d45184b5674b6a2202c2.data");
            Assert.assertTrue(data.size() > 0);
          }
        });

    // run command line with arguments
    ClassLoader classLoader = getClass().getClassLoader();
    URL project = classLoader.getResource("test_projects/Fifa2019");

    Main.main(
        new String[] {
          "test", "-p", project.getPath(), "5d31d45184b5674b6a2202c2",
        });
  }

  @Test
  public void testRunDataViewer() {
    String stepId = "5d31d26784b5674b6a2202b8";

    exit.expectSystemExitWithStatus(0);

    exit.checkAssertionAfterwards(
        new Assertion() {
          public void checkAssertion() {
            String json = systemOutRule.getLog();
            // TODO: test stdout here
            int code = JsonPath.read(json, "$.code");
            Assert.assertEquals(200, code);

            String jobId = JsonPath.read(json, "$.data.jobid");
            Assert.assertNotNull(jobId);

            // check data viewer job result
            String result =
                DataViewerHelper.getViewerJobDataWithDefaultDB(
                    "SELECT * FROM PCOLLECTION", jobId, stepId);
            int resultCode = JsonPath.read(result, "$.code");
            Assert.assertEquals(200, resultCode);

            List<Map<String, String>> fields = JsonPath.read(result, "$.data.schema.fields");
            Assert.assertTrue(fields.size() > 0);

            List<Object> data = JsonPath.read(result, "$.data.data");
            Assert.assertTrue(data.size() > 0);

            System.out.println(result);
          }
        });

    ClassLoader classLoader = getClass().getClassLoader();
    URL project = classLoader.getResource("test_projects/Fifa2019");

    Main.main(
        new String[] {
          "run", "-p", project.getPath(), stepId,
        });
  }

  @Test
  public void exploreExcelDataSource() {
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
    URL project = classLoader.getResource("test_projects/Fifa2019");

    Main.main(
        new String[] {
          "explore", "-p", project.getPath(), "5d4621fc3cac3c7b79a63694", "-n", "0", "--size", "5"
        });
  }

  @Test
  public void testExcelRun() {
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
    URL project = classLoader.getResource("test_projects/Fifa2019");

    Main.main(
            new String[] {
                    "run", "-p", project.getPath(), "5d46a60c518ac74593066c80",
                    });
  }


}
