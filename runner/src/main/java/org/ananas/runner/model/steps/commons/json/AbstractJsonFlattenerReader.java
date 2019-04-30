package org.ananas.runner.model.steps.commons.json;

import org.ananas.runner.model.steps.commons.ErrorHandler;
import org.ananas.runner.model.steps.commons.RowConverter;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public abstract class AbstractJsonFlattenerReader<T> extends PTransform<PCollection<T>, PCollection<Row>> implements Serializable {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractJsonFlattenerReader.class);
	private static final long serialVersionUID = -796443485928399659L;
	protected ErrorHandler errors;
	RowConverter converter;

	AbstractJsonFlattenerReader(RowConverter converter, ErrorHandler errors) {
		this.errors = errors;
		this.converter = converter;
	}

	public abstract Row document2BeamRow(T doc);

	@Override
	public PCollection<Row> expand(PCollection<T> input) {
		return input.apply(
				ParDo.of(
						new DoFn<T, Row>() {
							private static final long serialVersionUID = -7364417564214230507L;

							@ProcessElement
							public void processElement(ProcessContext ctx) {
								T doc = ctx.element();
								Row r = document2BeamRow(doc);
								if (r != null) {
									ctx.output(r);
								}
							}
						}));
	}
}
