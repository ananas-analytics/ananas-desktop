package org.ananas.acceptance;

import com.jayway.jsonpath.JsonPath;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.ananas.TestHelper;
import org.ananas.acceptance.helper.AcceptanceTestBase;
import org.ananas.cli.Main;
import org.junit.Assert;
import org.junit.Test;
import org.junit.contrib.java.lang.system.Assertion;

public class MongoSource extends AcceptanceTestBase {
  @Test
  public void exploreMySQLSource() {
    exit.expectSystemExitWithStatus(0);

    URL project = TestHelper.getResource("test_projects/mongo");

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
          "5d627ed8429d9752a3f43e8b",
          "-n",
          "0",
          "--size",
          "5",
          "-m",
          "HOST=" + props.getProperty("mongo.host"),
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
                JsonPath.read(json, "$.data.5d62811a429d9752a3f43e8f.schema.fields");
            Assert.assertTrue(fields.size() > 0);

            List<Object> data = JsonPath.read(json, "$.data.5d62811a429d9752a3f43e8f.data");
            Assert.assertTrue(data.size() > 0);
          }
        });

    // run command line with arguments
    ClassLoader classLoader = getClass().getClassLoader();
    URL project = classLoader.getResource("test_projects/mongo");

    Main.main(
        new String[] {
          "test",
          "-p",
          project.getPath(),
          "5d62811a429d9752a3f43e8f",
          "-m",
          "HOST=" + props.getProperty("mongo.host")
        });
  }
}
