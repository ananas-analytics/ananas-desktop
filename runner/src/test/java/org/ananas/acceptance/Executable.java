package org.ananas.acceptance;

import com.jayway.jsonpath.JsonPath;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.ananas.acceptance.helper.AcceptanceTestBase;
import org.ananas.cli.Main;
import org.junit.Assert;
import org.junit.Test;
import org.junit.contrib.java.lang.system.Assertion;

public class Executable extends AcceptanceTestBase {

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
                JsonPath.read(json, "$.data.5d68068f837d2272849eee0a.schema.fields");
            Assert.assertTrue(fields.size() > 0);

            List<Object> data = JsonPath.read(json, "$.data.5d68068f837d2272849eee0a.data");
            Assert.assertTrue(data.size() > 0);
          }
        });

    // run command line with arguments
    ClassLoader classLoader = getClass().getClassLoader();
    URL project = classLoader.getResource("test_projects/exe");

    Main.main(
        new String[] {
          "test", "-p", project.getPath(), "5d68068f837d2272849eee0a",
        });
  }
}
