package org.ananas.runner.model.errors;

import org.apache.commons.lang3.tuple.MutablePair;

public class DatumaniaException extends RuntimeException {

	private static final long serialVersionUID = 7079719702593375677L;
	public MutablePair<ExceptionHandler.ErrorCode, String> error;

	public DatumaniaException(MutablePair<ExceptionHandler.ErrorCode, String> error) {
		this.error = error;
	}

	public DatumaniaException(ExceptionHandler.ErrorCode code, String error) {
		this.error = MutablePair.of(code, error);
	}

}
