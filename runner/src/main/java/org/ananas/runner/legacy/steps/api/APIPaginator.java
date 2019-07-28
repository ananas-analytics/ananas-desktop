package org.ananas.runner.legacy.steps.api;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
  protected String payload;

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
        Object result = JsonPath.parse(json).read( APIConfig.jsonPath);
        this.payload = JsonUtil.toJson(result, false);
        return JsonAutodetect.autodetectJson(this.payload, false);
      } else {
        this.payload = json;
        return Schema.builder().addStringField("TEXT").build();
      }
    }


  private boolean isJson() {
    return "json".equals(APIConfig.format);
  }

  @Override
  public Iterable<Row> iterateRows(Integer page, Integer pageSize) {
    if (isJson()) {
      JsonStringBasedFlattenerReader jsonReader =
              new JsonStringBasedFlattenerReader(SchemaBasedRowConverter.of(schema), new ErrorHandler());
      return Arrays.asList(this.payload.split(this.APIConfig.delimiter)).stream().map(
              line -> jsonReader.document2BeamRow(line)).collect(Collectors.toList());
    } else {
      return Arrays.asList(this.payload.split(this.APIConfig.delimiter)).stream().map(
              line -> Row.withSchema(this.schema).addValues(line).build()).collect(Collectors.toList());
    }
  }

  private String handle() throws IOException {

    Map<String, String> headers = new HashMap<>();

    if (isJson()) {
      headers.put("Content-Type", "application/json");
    }
    switch (APIConfig.method.toUpperCase()) {
      case "GET":
        return HttpClient.GET(
                APIConfig.url,
                headers,
            conn -> {
              return IOUtils.toString(conn.getInputStream());
            });
      case "POST":
        return HttpClient.POST(
                APIConfig.url,
                headers,
                APIConfig.body,
            conn -> {
              return IOUtils.toString(conn.getInputStream());
            });
      case "PUT":
        return HttpClient.PUT(
                APIConfig.url,
                headers,
                APIConfig.body,
            conn -> {
              return IOUtils.toString(conn.getInputStream());
            });
      default:
        throw new IllegalStateException("Unsupported HTTP method '" + APIConfig.method + "'");
    }
  }





}
