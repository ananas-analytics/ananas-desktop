package org.ananas.runner.misc;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import org.ananas.runner.kernel.common.JsonUtil;

public class HttpClient {

  public static <T> T handle(
      URL url, Map<String, String> params, String method, Object body, RequestHandler<T> handler)
      throws IOException {
    // #https://cloud.google.com/appengine/docs/standard/java/issue-requests
    // #GET http://localhost:3000/api/v1/pipeline/$PIPELINE_ID/steps
    HttpURLConnection conn = null;
    try {
      conn = (HttpURLConnection) url.openConnection();
      conn.setDoOutput(true);
      conn.setRequestMethod(method);
      if (params != null) {
        for (Map.Entry<String, String> param : params.entrySet()) {
          conn.setRequestProperty(param.getKey(), param.getValue());
        }
      }
      if (body != null && (method.equalsIgnoreCase("POST") || method.equalsIgnoreCase("PUT"))) {
        conn.setRequestProperty("Content-Type", "application/json");
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(JsonUtil.toJson(body));
        wr.flush();
        wr.close();
      }

      if (conn.getResponseCode() != 200) {
        throw new RuntimeException(
            "Cannot connect to "
                + url
                + " \nHTTP  "
                + method
                + " "
                + conn.getResponseCode()
                + " Msg : "
                + conn.getResponseMessage());
      }
    } catch (Exception e) {
      throw new RuntimeException(
          "Cannot connect to "
              + url
              + " \nHTTP  "
              + method
              + " "
              + conn.getResponseCode()
              + " Msg : "
              + conn.getResponseMessage());
    }
    try {
      return handler.handler(conn);
    } finally {
      conn.disconnect();
    }
  }

  public static <T> T PUT(
      String uri, Map<String, String> params, Object body, RequestHandler<T> handler)
      throws IOException {
    URL url = new URL(uri);
    return handle(url, params, "PUT", body, handler);
  }

  public static <T> T POST(
      String uri, Map<String, String> params, Object body, RequestHandler<T> handler)
      throws IOException {
    URL url = new URL(uri);
    return handle(url, params, "POST", body, handler);
  }

  public static <T> T GET(String uri, Map<String, String> params, RequestHandler<T> handler)
      throws IOException {
    URL url = new URL(uri);
    return handle(url, params, "GET", null, handler);
  }

  public interface RequestHandler<T> {
    T handler(HttpURLConnection conn) throws IOException;
  }
}
