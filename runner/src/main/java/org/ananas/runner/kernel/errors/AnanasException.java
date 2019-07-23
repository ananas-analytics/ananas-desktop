package org.ananas.runner.kernel.errors;

import org.ananas.runner.kernel.errors.ExceptionHandler.ErrorCode;
import org.apache.commons.lang3.tuple.MutablePair;

public class AnanasException extends RuntimeException {

  private static final long serialVersionUID = 7079719702593375677L;
  public MutablePair<ErrorCode, String> error;

  public static final String DEFAULT_ERROR_MSG = "Oops, something went wrong.";

  public AnanasException(MutablePair<ErrorCode, String> error) {
    super(error.getRight());
    this.error = error;
  }

  public AnanasException(ErrorCode code, String error) {
    super(error);
    if (error == null) {
      this.error = MutablePair.of(code, DEFAULT_ERROR_MSG);
    } else {
      this.error = MutablePair.of(code, error);
    }
  }

  public AnanasException(ErrorCode code) {
    super(DEFAULT_ERROR_MSG);
    this.error = MutablePair.of(code, DEFAULT_ERROR_MSG);
  }
}
