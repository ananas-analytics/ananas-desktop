package org.ananas.runner.kernel.errors;

import org.ananas.runner.kernel.errors.ExceptionHandler.ErrorCode;
import org.apache.commons.lang3.tuple.MutablePair;

public class AnanasException extends RuntimeException {

  private static final long serialVersionUID = 7079719702593375677L;
  public MutablePair<ErrorCode, String> error;

  public AnanasException(MutablePair<ErrorCode, String> error) {
    this.error = error;
  }

  public AnanasException(ErrorCode code, String error) {
    this.error = MutablePair.of(code, error);
  }
}
