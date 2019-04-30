package org.ananas.runner.model.steps.commons;

import org.apache.beam.sdk.schemas.Schema;

public class AbstractStepLoader extends AbstractStepRunner {

	private static final long serialVersionUID = -3620558801376183529L;

	protected AbstractStepLoader() {
		super(StepType.Loader);
	}

	@Override
	public DataReader getReader() {
		return NullDataReader.of();
	}

	@Override
	public Schema getSchema() {
		return Schema.builder().build();
	}

	@Override
	public void setReader() {
		//NO OPER
	}

}
