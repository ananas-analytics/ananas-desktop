package org.ananas.runner.model.errors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.beam.repackaged.beam_sdks_java_extensions_sql.org.apache.calcite.sql.parser.SqlParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;

public class ExceptionHandler {

  public enum ErrorCode {
    SQL(1),
    GENERAL(2),
    JAVASCRIPT(3),
    CONNECTION(4),
    DAG(5);

    public int code;

    ErrorCode(int i) {
      this.code = i;
    }
  }

  public static AnanasException valueOf(ErrorCode code, String message) {
    return new AnanasException(MutablePair.of(code, message));
  }

  public static AnanasException valueOf(Throwable e) {
    return new AnanasException(findRootCauseMessage(e));
  }

  public static MutablePair<ErrorCode, String> findRootCauseMessage(Throwable e) {
    while (e != null && e.getCause() != null) {
      MutablePair<ErrorCode, String> error = parseException(e);
      if (error != null) {
        return error;
      }
      e = e.getCause();
    }
    if (e instanceof AnanasException) {
      return ((AnanasException) e).error;
    }
    return MutablePair.of(
        ErrorCode.GENERAL, StringUtils.defaultString(e.getMessage(), e.toString()));
  }

  private static MutablePair<ErrorCode, String> parseException(Throwable e) {
    if (e instanceof AnanasException) {
      return ((AnanasException) e).error;
    }
    if (e instanceof NullPointerException) {
      return MutablePair.of(ErrorCode.GENERAL, "Oops. Something went wrong.");
    }
    if (e.getCause() instanceof NullPointerException) {
      for (StackTraceElement i : e.getCause().getStackTrace()) {
        if (i.getMethodName().contains("compare")) {
          return MutablePair.of(
              ErrorCode.SQL,
              "can't compare nullable column. Check if it is null using 'is null' operator. You can also use emptyIfNull(colname), zeroIfNull(colname) or falseIfNull(colname) expression instead");
        }
      }
      return MutablePair.of(ErrorCode.GENERAL, "Oops. Something went wrong.");
    }
    if (e.getCause() instanceof SqlParseException) {
      return MutablePair.of(ErrorCode.SQL, enrichMessage(e.getCause().getMessage()));
    }
    if (e.getCause() instanceof ClassCastException) {
      return MutablePair.of(ErrorCode.SQL, e.getCause().getMessage().replace("java.lang.", ""));
    }
    return null;
  }

  private static String enrichMessage(String msg) {
    String pattern = "\\s*Encountered \",?\\s*(.*\\..*).*\"";
    Pattern p = Pattern.compile(pattern);
    Matcher matcher = p.matcher(msg);
    if (matcher.find()) {
      String str = matcher.group(1);
      return String.format("Did you mean '%s'? %s", str, msg);
    }
    return msg;
  }
}
