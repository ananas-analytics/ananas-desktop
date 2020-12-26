package org.ananas.runner.steprunner.api;

import com.github.wnameless.json.flattener.FlattenMode;
import com.jayway.jsonpath.JsonPath;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import org.ananas.runner.core.common.JsonStringBasedFlattenerReader;
import org.ananas.runner.core.common.JsonUtil;
import org.ananas.runner.core.errors.AnanasException;
import org.ananas.runner.core.errors.ErrorHandler;
import org.ananas.runner.core.errors.ExceptionHandler;
import org.ananas.runner.core.paginate.LoopedAutoDectectedSchemaPaginator;
import org.ananas.runner.core.schema.JsonAutodetect;
import org.ananas.runner.core.schema.SchemaBasedRowConverter;
import org.ananas.runner.misc.HttpClient;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;
import org.apache.commons.lang3.tuple.MutablePair;
import spark.utils.IOUtils;

public class APIPaginator extends LoopedAutoDectectedSchemaPaginator {

  private APIStepConfig APIConfig;
  protected ErrorHandler errors;
  protected String jsonPayloadAsString;
  protected Object jsonPayloadAsObject;

  public APIPaginator(String id, String type, Map<String, Object> config, Schema schema) {
    super(id, type, config, schema);
    this.errors = new ErrorHandler();
  }

  @Override
  public Schema autodetectRawSchema() {
    // get first loop config
    this.APIConfig = new APIStepConfig(this.configGenerator.getConfig());
    String json = "";
    try {
      json = handle();
    } catch (Exception e) {
      throw new AnanasException(
          MutablePair.of(ExceptionHandler.ErrorCode.CONNECTION, e.getMessage()));
    }
    if (isJsonLog()) {
      Map<String, Schema.FieldType> types = new HashMap<>();
      this.jsonPayloadAsString = json;
      this.jsonPayloadAsObject = new ArrayList();
      Arrays.asList(this.jsonPayloadAsString.split("\n"))
          .forEach(
              line -> {
                try {
                  Object jsonObj = JsonPath.parse(line).read(APIConfig.jsonPath);
                  ((List) this.jsonPayloadAsObject).add(jsonObj);
                  JsonAutodetect.inferTypes(
                      types, JsonUtil.toJson(jsonObj, false), FlattenMode.KEEP_ARRAYS, false);
                } catch (Exception e) {
                  this.errors.addError(e);
                }
              });
      return JsonAutodetect.buildSchema(types);
    } else if (isJson()) {
      this.jsonPayloadAsObject = JsonPath.parse(json).read(APIConfig.jsonPath);
      this.jsonPayloadAsString = JsonUtil.toJson(this.jsonPayloadAsObject, false);
      if (isJsonArray()) {
        Map<String, Schema.FieldType> types = new HashMap<>();
        List l = (List) this.jsonPayloadAsObject;
        for (Object e : l) {
          JsonAutodetect.inferTypes(
              types, JsonUtil.toJson(e, false), FlattenMode.KEEP_ARRAYS, false);
        }
        return JsonAutodetect.buildSchema(types);
      } else {
        return JsonAutodetect.autodetectJson(
            JsonUtil.toJson(this.jsonPayloadAsObject, false), false);
      }
    } else {
      this.jsonPayloadAsString = json;
      return Schema.builder().addStringField("TEXT").build();
    }
  }

  private boolean isJsonArray() {
    return isJsonLog() || List.class.isAssignableFrom(this.jsonPayloadAsObject.getClass());
  }

  private boolean isJsonLog() {
    return "jsonlog".equals(APIConfig.format);
  }

  private boolean isJson() {
    return "json".equals(APIConfig.format);
  }

  @Override
  public Iterable<Row> iterateRawRows(Integer page, Integer pageSize) {
    this.configGenerator.reset();
    for (int i = 0; i < page; i++) {
      if (this.configGenerator.hasNext()) {
        this.configGenerator.prepareNext();
      } else {
        return Collections.emptyList().stream()
            .map(line -> Row.withSchema(this.rawSchema).addValues(line).build())
            .collect(Collectors.toList());
      }
    }
    this.APIConfig = new APIStepConfig(this.configGenerator.getConfig());
    if (isJson() || isJsonLog()) {
      JsonStringBasedFlattenerReader jsonReader =
          new JsonStringBasedFlattenerReader(
              SchemaBasedRowConverter.of(rawSchema), new ErrorHandler());
      if (isJsonArray())
        return ((List<Object>) this.jsonPayloadAsObject)
            .stream()
                .map(o -> jsonReader.document2BeamRow(JsonUtil.toJson(o, false)))
                // .limit(5000)
                .collect(Collectors.toList());
      else
        return Arrays.asList(this.jsonPayloadAsString.split(this.APIConfig.delimiter)).stream()
            .map(line -> jsonReader.document2BeamRow(line))
            .collect(Collectors.toList());
    } else {
      return Arrays.asList(this.jsonPayloadAsString.split(this.APIConfig.delimiter)).stream()
          .map(line -> Row.withSchema(this.rawSchema).addValues(line).build())
          .collect(Collectors.toList());
    }
  }

  private String handle() throws IOException {
    if (isJson()) {
      APIConfig.headers.put("Content-Type", "application/json");
    }

    switch (APIConfig.method.toUpperCase()) {
      case "GET":
        return HttpClient.GET(
            APIConfig.url,
            APIConfig.headers,
            conn -> {
              return IOUtils.toString(conn.getInputStream());
            });
      case "POST":
        return HttpClient.POST(
            APIConfig.url,
            APIConfig.headers,
            APIConfig.body,
            conn -> {
              return IOUtils.toString(conn.getInputStream());
            });
      case "PUT":
        return HttpClient.PUT(
            APIConfig.url,
            APIConfig.headers,
            APIConfig.body,
            conn -> {
              return IOUtils.toString(conn.getInputStream());
            });
      default:
        throw new IllegalStateException("Unsupported HTTP method '" + APIConfig.method + "'");
    }
  }
}
