package org.ananas.runner.model.steps.files;


import org.ananas.runner.model.steps.commons.ErrorHandler;
import org.ananas.runner.model.steps.commons.paginate.AbstractPaginator;
import org.apache.beam.sdk.schemas.Schema;

public abstract class AbstractFilePaginator extends AbstractPaginator {

	protected String url;
	protected ErrorHandler errors;

	public AbstractFilePaginator(String id, String url) {
		super(id, null);
		this.url = url;
		this.errors = new ErrorHandler();
		this.schema = null;
	}

	protected abstract Schema autodetect(Integer pageSize);

}
