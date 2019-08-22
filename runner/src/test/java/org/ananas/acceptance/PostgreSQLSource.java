package org.ananas.acceptance;

import com.jayway.jsonpath.JsonPath;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.ananas.TestHelper;
import org.ananas.acceptance.helper.AcceptanceTestBase;
import org.ananas.acceptance.helper.DataViewerHelper;
import org.ananas.cli.Main;
import org.junit.Assert;
import org.junit.Test;
import org.junit.contrib.java.lang.system.Assertion;

public class PostgreSQLSource extends AcceptanceTestBase {
  @Test
  public void explorePostgresSource() {
    exit.expectSystemExitWithStatus(0);

    URL project = TestHelper.getResource("test_projects/postgres");

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
          "5d58319089823537bb919b59",
          "-n",
          "0",
          "--size",
          "5",
          "-m",
          "HOST=" + props.getProperty("postgres.host"),
          "-m",
          "USER=" + props.getProperty("postgres.user"),
          "-m",
          "PASSWORD=" + props.getProperty("postgres.password"),
        });
  }

  @Test
  public void testRunDataViewer() {
    String stepId = "5d58405642ad63660c333620";

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

    URL project = TestHelper.getResource("test_projects/postgres");

    Main.main(
        new String[] {
          "run",
          "-p",
          project.getPath(),
          stepId,
          "-m",
          "HOST=" + props.getProperty("postgres.host"),
          "-m",
          "USER=" + props.getProperty("postgres.user"),
          "-m",
          "PASSWORD=" + props.getProperty("postgres.password"),
        });
  }

  @Test
  public void testDatatypes_Explore() {
    exit.expectSystemExitWithStatus(0);

    URL project = TestHelper.getResource("test_projects/postgres");

    exit.checkAssertionAfterwards(
        new Assertion() {
          public void checkAssertion() {
            String json = systemOutRule.getLog();
            int code = JsonPath.read(json, "$.code");
            Assert.assertEquals(200, code);

            List<Map<String, String>> fields = JsonPath.read(json, "$.data.schema.fields");
            Assert.assertTrue(fields.size() > 0);

            List<String> data = JsonPath.read(json, "$.data.data[0]");

            Assert.assertEquals(
                "pgsql xml",
                "<book><title>Manual</title><chapter>...</chapter></book>",
                data.get(0));

            Assert.assertEquals(35, data.size());

            for (int i = 0; i < data.size(); i++) {
              // System.out.println(i + " XXX " + String.valueOf(data.get(i)));
              Assert.assertNotNull(data.get(i));
              Assert.assertFalse(String.valueOf(data.get(i)).isEmpty());
            }
          }
        });

    Main.main(
        new String[] {
          "explore",
          "-p",
          project.getPath(),
          "5d5b7d795ebce4675573dca3",
          "-n",
          "0",
          "--size",
          "5",
          "-m",
          "HOST=" + props.getProperty("postgres.host"),
          "-m",
          "USER=" + props.getProperty("postgres.user"),
          "-m",
          "PASSWORD=" + props.getProperty("postgres.password"),
        });
  }
}
