package org.ananas.runner.core.common;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import java.io.IOException;
import java.io.InputStream;
import org.ananas.server.ApiResponse;

public class JsonUtil {

  public static String toJson(Object object, boolean indentOutput) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JodaModule());
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    objectMapper.configure(SerializationFeature.FLUSH_AFTER_WRITE_VALUE, true);
    objectMapper.configure(SerializationFeature.INDENT_OUTPUT, indentOutput);
    objectMapper.configure(
        com.fasterxml.jackson.core.JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
    try {
      return indentOutput
          ? objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object)
          : objectMapper.writeValueAsString(object);
    } catch (IOException e) {
      return e.getMessage();
    }
  }

  public static String toJson(Object object) {
    return toJson(object, false);
  }

  public static <T> T fromJson(String in, Class<T> type) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readerFor(type).readValue(in);
  }

  public static <T> T fromJson(InputStream in, Class<T> type) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readerFor(type).readValue(in);
  }

  public static <T> ApiResponse<T> fromJsonToApiResponse(String in, Class<T> type)
      throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    JavaType dataType =
        objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, type);
    return objectMapper.readerFor(dataType).readValue(in);
  }

  public static <T> ApiResponse<T> fromJsonToApiResponse(InputStream in, Class<T> type)
      throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    JavaType dataType =
        objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, type);
    return objectMapper.readerFor(dataType).readValue(in);
  }
}
