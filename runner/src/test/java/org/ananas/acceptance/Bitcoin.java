package org.ananas.acceptance;

import com.jayway.jsonpath.JsonPath;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ananas.acceptance.helper.AcceptanceTestBase;
import org.ananas.acceptance.helper.DataViewerHelper;
import org.ananas.cli.Main;
import org.ananas.runner.core.model.Engine;
import org.junit.Assert;
import org.junit.Test;
import org.junit.contrib.java.lang.system.Assertion;

public class Bitcoin extends AcceptanceTestBase {
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

  @Test
  public void testRunDataViewerOnSpark() throws IOException, URISyntaxException {
    String stepId = "5d4559249c7a5441fdc67d47";

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

            Engine engine = new Engine();
            engine.name = "test spark engine";
            engine.type = "Spark";
            engine.properties = new HashMap<>();
            engine.properties.put("app_name", "test app on spark");
            engine.properties.put("database_type", "mysql");
            engine.properties.put(
                "database_url",
                "mysql://" + props.getProperty("mysql.host") + ":3306/classicmodels");
            engine.properties.put("database_user", "root");
            engine.properties.put("database_password", props.getProperty("mysql.password"));
            engine.properties.put(
                "sparkMaster", "spark://" + props.getProperty("spark.master") + ":7707");
            engine.properties.put("enableMetricSinks", "true");
            engine.properties.put("streaming", "false");
            engine.properties.put("tempLocation", "/tmp/");

            // check data viewer job result
            String result =
                DataViewerHelper.getViewerJobDataWithEngine(
                    "SELECT * FROM PCOLLECTION", jobId, stepId, engine);
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
    URL project = classLoader.getResource("test_projects/bitcoin");

    Main.main(
        new String[] {
          "run",
          "-p",
          project.getPath(),
          stepId,
          "-f",
          project.getPath() + "/spark_profile.yml",
          "-m",
          "MYSQL_HOST=" + props.getProperty("mysql.host"),
          "-m",
          "MYSQL_PASSWORD=" + props.getProperty("mysql.password"),
          "-m",
          "MYSQL_DB=classicmodels",
          "-m",
          "SPARK_MASTER_HOST=" + props.getProperty("spark.master"),
        });
  }

  /*
  @Test
  public void testRunDataViewerOnFlink() throws IOException, URISyntaxException {
    String stepId = "5d4559249c7a5441fdc67d47";

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

            Engine engine = new Engine();
            engine.name = "test flink engine";
            engine.type = "Flink";
            engine.properties = new HashMap<>();
            engine.properties.put("app_name", "test app on flink");
            engine.properties.put("database_type", "mysql");
            engine.properties.put(
                "database_url",
                "mysql://" + props.getProperty("mysql.host") + ":3306/classicmodels");
            engine.properties.put("database_user", "root");
            engine.properties.put("database_password", props.getProperty("mysql.password"));
            engine.properties.put("flinkMaster", props.getProperty("flink.master") + ":6123");

            // check data viewer job result
            String result =
                DataViewerHelper.getViewerJobDataWithEngine(
                    "SELECT * FROM PCOLLECTION", jobId, stepId, engine);
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
    URL project = classLoader.getResource("test_projects/bitcoin");

    Main.main(
        new String[] {
          "run",
          "-p",
          project.getPath(),
          stepId,
          "-f",
          project.getPath() + "/flink_profile.yml",
          "-m",
          "MYSQL_HOST=" + props.getProperty("mysql.host"),
          "-m",
          "MYSQL_PASSWORD=" + props.getProperty("mysql.password"),
          "-m",
          "MYSQL_DB=classicmodels",
          "-m",
          "FLINK_MASTER_HOST=" + props.getProperty("flink.master"),
        });
  }
   */
}
