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
      Path path = Paths.get(current.toURI()).resolve("../../../../../cli/ananas-cli-latest.jar");
      return path.toRealPath().toAbsolutePath().toString();
    } catch (URISyntaxException | IOException e) {
      System.err.println(
          "Can't find compiled cli file, please run `./build-cli.sh [JDK_HOME]` to generate jar file");
    }
    return "mock.jar";
  }
}
