package org.ananas.runner.misc;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import org.ananas.runner.core.common.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClient {

  private static final Logger LOG = LoggerFactory.getLogger(HttpClient.class);

  public static <T> T handle(
      URL url, Map<String, String> params, String method, Object body, RequestHandler<T> handler)
      throws IOException {
    // #https://cloud.google.com/appengine/docs/standard/java/issue-requests
    HttpURLConnection conn = null;
    try {
      conn = (HttpURLConnection) url.openConnection();
      conn.setDoOutput(true);
      conn.setRequestMethod(method);
      if (params != null) {
        for (Map.Entry<String, String> param : params.entrySet()) {
          LOG.debug("Setting param {} ", param.toString());
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
        LOG.error(
            "{} {} [params={}, body= {}] status ",
            method,
            url.toString(),
            params,
            body,
            conn.getResponseCode());
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
      LOG.error(
          "{} {} [params={}, body= {}] error : ",
          method,
          url.toString(),
          params,
          body,
          e.getStackTrace());
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
      LOG.debug(
          "HTTP Request - {} {} [params={}, body= {}] status OK",
          method,
          url.toString(),
          params,
          body);
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
