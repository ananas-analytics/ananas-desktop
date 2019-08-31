package org.ananas.server;

import java.util.HashMap;
import java.util.Map;
import org.ananas.runner.core.errors.AnanasException;
import org.ananas.runner.core.errors.ExceptionHandler;
import org.apache.commons.lang3.tuple.MutablePair;

public class ApiResponseBuilder {

  public static final String CODE = "code";
  public static final String MESSAGE = "message";
  public static final String DATA = "data";

  Map<String, Object> resp = new HashMap<>();

  public ApiResponseBuilder() {
    this.resp = new HashMap<>();
  }

  public static ApiResponseBuilder Of() {
    return new ApiResponseBuilder();
  }

  public ApiResponseBuilder OK(Object data) {
    this.resp.put(CODE, 200);
    if (data != null) {
      this.resp.put(DATA, data);
    } else {
      this.resp.put(DATA, "");
    }
    return this;
  }

  public Map<String, Object> build() {
    return this.resp;
  }

  public ApiResponseBuilder KO(Exception e) {
    if (e instanceof AnanasException) {
      this.resp.put(CODE, ((AnanasException) e).error.getLeft().code);
      this.resp.put(MESSAGE, ((AnanasException) e).error.getRight());
      return this;
    }

    MutablePair<ExceptionHandler.ErrorCode, String> error =
        ExceptionHandler.findRootCauseMessage(e);
    this.resp.put(CODE, error.getLeft().code);
    this.resp.put(MESSAGE, error.getRight());
    return this;
  }
}
