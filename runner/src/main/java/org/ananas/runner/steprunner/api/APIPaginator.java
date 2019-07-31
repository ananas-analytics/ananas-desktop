package org.ananas.runner.steprunner.api;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.github.wnameless.json.flattener.FlattenMode;
import com.jayway.jsonpath.JsonPath;
import org.ananas.runner.kernel.common.JsonStringBasedFlattenerReader;
import org.ananas.runner.kernel.common.JsonUtil;
import org.ananas.runner.kernel.errors.AnanasException;
import org.ananas.runner.kernel.errors.ErrorHandler;
import org.ananas.runner.kernel.errors.ExceptionHandler;
import org.ananas.runner.kernel.paginate.AutoDetectedSchemaPaginator;
import org.ananas.runner.kernel.schema.JsonAutodetect;
import org.ananas.runner.kernel.schema.SchemaBasedRowConverter;
import org.ananas.runner.misc.HttpClient;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;
import org.apache.commons.lang3.tuple.MutablePair;
import spark.utils.IOUtils;

public class APIPaginator extends AutoDetectedSchemaPaginator {


  private APIStepConfig APIConfig;
  protected ErrorHandler errors;
  protected String jsonPayloadAsString;
  protected Object jsonPayloadAsObject;

  public APIPaginator(String id, String type, Map<String, Object> config, Schema schema) {
    super(id, type, config, schema);
    this.errors = new ErrorHandler();
  }

    @Override
    public void parseConfig(Map<String, Object> config) {
      this.APIConfig = new APIStepConfig(config);
    }


    @Override
    public Schema autodetect() {
      String json = "";
      try {
        json = handle();
      } catch (Exception e) {
        throw new AnanasException(
                MutablePair.of(ExceptionHandler.ErrorCode.CONNECTION, e.getMessage()));
      }
      if (isJson()) {
        this.jsonPayloadAsObject = JsonPath.parse(json).read( APIConfig.jsonPath);
        this.jsonPayloadAsString = JsonUtil.toJson(this.jsonPayloadAsObject, false);
        if (isJsonArray()) {
          Map<String, Schema.FieldType> types = new HashMap<>();
          List l = (List)this.jsonPayloadAsObject;
          for (Object e : l) {
            JsonAutodetect.inferTypes(types, JsonUtil.toJson(e, false), FlattenMode.KEEP_ARRAYS, false);
          }
          return JsonAutodetect.buildSchema(types);
        } else {
          return JsonAutodetect.autodetectJson(this.jsonPayloadAsString, false);
        }
      } else {
        this.jsonPayloadAsString = json;
        return Schema.builder().addStringField("TEXT").build();
      }
    }

  private boolean isJsonArray() {
    return List.class.isAssignableFrom(this.jsonPayloadAsObject.getClass());
  }


  private boolean isJson() {
    return "json".equals(APIConfig.format);
  }

  @Override
  public Iterable<Row> iterateRows(Integer page, Integer pageSize) {
    if (isJson()) {
      JsonStringBasedFlattenerReader jsonReader =
              new JsonStringBasedFlattenerReader(SchemaBasedRowConverter.of(schema), new ErrorHandler());
      if (isJsonArray())
        return ((List<Object>)this.jsonPayloadAsObject).stream().map(
                o -> jsonReader.document2BeamRow(JsonUtil.toJson(o, false))).collect(Collectors.toList());
      else
        return Arrays.asList(this.jsonPayloadAsString.split(this.APIConfig.delimiter)).stream().map(
              line -> jsonReader.document2BeamRow(line)).collect(Collectors.toList());
    } else {
      return Arrays.asList(this.jsonPayloadAsString.split(this.APIConfig.delimiter)).stream().map(
              line -> Row.withSchema(this.schema).addValues(line).build()).collect(Collectors.toList());
    }
  }

  private String handle() throws IOException {


    if (isJson()) {
      APIConfig.headers.put("Content-Type", "application/json");
    }

    System.out.println("headers" + APIConfig.headers.entrySet());
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
