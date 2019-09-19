package org.ananas;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class TestHelper {
  public static URL getResource(String relativePath) {
    ClassLoader classLoader = TestHelper.class.getClassLoader();
    URL resource = classLoader.getResource(relativePath);

    return resource;
  }

  public static String getTestPropertyFile() {
    String value = System.getProperty("ananas.test.properties");
    if (value == null) {
      value = "test.local.properties";
    }
    return value;
  }

  public static Properties loadProperties(String relativePropertyPath) {
    URL resource = getResource(relativePropertyPath);
    try (InputStream input = new FileInputStream(resource.getPath())) {
      Properties prop = new Properties();
      prop.load(input);
      return prop;

    } catch (IOException ex) {
      ex.printStackTrace();
    }
    return null;
  }

  public static String getCLIPath() {
    try {
      URL current = getResource("");
      Path cliPath = Paths.get(current.toURI()).resolve("../../../../../cli/ananas-cli-latest.jar");
      Path sparkPath =
          Paths.get(current.toURI())
              .resolve("../../../../../runner/build/libs/ananas-spark-latest.jar");

      String path =
          cliPath.toRealPath().toAbsolutePath().toString()
              + ";"
              + sparkPath.toRealPath().toAbsolutePath().toString();
      System.out.println(path);
      return path;
    } catch (URISyntaxException | IOException e) {
      e.printStackTrace();
      System.err.println(
          "Can't find compiled cli file, please run `./build-cli.sh [JDK_HOME]` to generate jar file");
    }
    return "mock.jar";
  }
}
