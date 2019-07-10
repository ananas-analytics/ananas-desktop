package org.ananas.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileInputStream;
import java.io.IOException;
import spark.utils.IOUtils;

public class YamlHelper {
  public static <T> T openYAML(String path, Class<T> clazz) throws IOException {
    FileInputStream in = new FileInputStream(path);
    String yaml = IOUtils.toString(in);
    ObjectMapper yamlReader =
        new ObjectMapper(new com.fasterxml.jackson.dataformat.yaml.YAMLFactory());
    return yamlReader.readValue(yaml, clazz);
  }
}
