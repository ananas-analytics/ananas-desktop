package org.ananas.runner.misc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import spark.utils.IOUtils;

public class YamlHelper {
  public static <T> T openYAML(String path, Class<T> clazz) throws IOException {
    FileInputStream in = new FileInputStream(path);
    String yaml = IOUtils.toString(in);
    ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
    return yamlReader.readValue(yaml, clazz);
  }

  public static <T> Map<String, T> openMapYAML(String path, Class<T> clazz) throws IOException {
    FileInputStream in = new FileInputStream(path);
    String yaml = IOUtils.toString(in);
    TypeReference<HashMap<String, T>> typeRef = new TypeReference<HashMap<String, T>>() {};
    ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
    return yamlReader.readValue(yaml, typeRef);
  }

  public static <T> T openYAML(InputStream in, Class<T> clazz) throws IOException {
    String yaml = IOUtils.toString(in);
    ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
    return yamlReader.readValue(yaml, clazz);
  }

  public static <T> void saveYAML(String path, T object) throws IOException {
    ObjectMapper yamlWriter =
        new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
    yamlWriter.writeValue(new File(path), object);
  }
}
