package org.ananas.runner.model.errors;

import org.apache.commons.lang3.tuple.MutablePair;

public class AnanasException extends RuntimeException {

	private static final long serialVersionUID = 7079719702593375677L;
	public MutablePair<ExceptionHandler.ErrorCode, String> error;

	public AnanasException(MutablePair<ExceptionHandler.ErrorCode, String> error) {
		this.error = error;
	}

	public AnanasException(ExceptionHandler.ErrorCode code, String error) {
		this.error = MutablePair.of(code, error);
	}

}
