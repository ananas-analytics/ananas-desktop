package org.ananas.runner.model.steps.commons;

import org.ananas.runner.model.errors.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class ErrorHandler implements Serializable {

	private static final Logger LOG = LoggerFactory.getLogger(ErrorHandler.class);
	private static final long serialVersionUID = -6241195255571854528L;

	private static int DEFAULT_MAX_ERRORS = Integer.MAX_VALUE;

	private AtomicInteger count;

	protected ErrorHandler(int maxError) {
		this.count = new AtomicInteger(0);
	}

	public ErrorHandler() {
		this(DEFAULT_MAX_ERRORS);
	}

	public void addError(ExceptionHandler.ErrorCode code, String message) {
		int c = this.count.incrementAndGet();
		LOG.debug(message);
		if (c >= DEFAULT_MAX_ERRORS) {
			throw ExceptionHandler.valueOf(code, message);
		}
	}

	public void addError(Exception e) {
		int c = this.count.incrementAndGet();
		LOG.debug(e.getMessage());
		if (c >= DEFAULT_MAX_ERRORS) {
			throw ExceptionHandler.valueOf(e);
		}
	}

}
