package org.ananas.runner.kernel.errors;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorHandler implements Serializable {

  private static final Logger LOG = LoggerFactory.getLogger(ErrorHandler.class);
  private static final long serialVersionUID = -6241195255571854528L;

  private static int DEFAULT_MAX_ERRORS = Integer.MAX_VALUE;

  private AtomicInteger count;
  private AtomicReference<String> lastErrorMessage;

  protected ErrorHandler(int maxError) {
    this.count = new AtomicInteger(0);
    this.lastErrorMessage = new AtomicReference<>("");
  }

  public ErrorHandler() {
    this(DEFAULT_MAX_ERRORS);
  }

  public void addError(ExceptionHandler.ErrorCode code, String message) {
    int c = this.count.incrementAndGet();
    this.lastErrorMessage.set(message);
    LOG.debug(message);
    if (c >= DEFAULT_MAX_ERRORS) {
      throw ExceptionHandler.valueOf(code, message);
    }
  }

  public void addError(Exception e) {
    int c = this.count.incrementAndGet();
    this.lastErrorMessage.set(e.getLocalizedMessage());
    LOG.debug(e.getMessage());
    if (c >= DEFAULT_MAX_ERRORS) {
      throw ExceptionHandler.valueOf(e);
    }
  }

  public boolean hasError() {
    return count.get() > 0;
  }

  @Override
  public String toString() {
    return "Encounter " + count.get() + " errors, lastErrorMessage=" + lastErrorMessage.get();
  }
}
