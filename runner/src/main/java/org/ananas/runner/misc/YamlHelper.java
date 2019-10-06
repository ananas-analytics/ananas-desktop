package org.ananas.runner.misc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import spark.utils.IOUtils;

public class YamlHelper {
  public static <T> T openYAML(String path, Class<T> clazz) throws IOException {
    FileInputStream in = new FileInputStream(path);
    String yaml = IOUtils.toString(in);
    ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
    return yamlReader.readValue(yaml, clazz);
  }

  public static <T> T openYAML(InputStream in, Class<T> clazz) throws IOException {
    String yaml = IOUtils.toString(in);
    ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
    return yamlReader.readValue(yaml, clazz);
  }
}
