package org.ananas.runner.model.steps.files.csv;

import org.ananas.runner.model.steps.commons.ErrorHandler;
import org.apache.beam.sdk.extensions.sql.impl.schema.BeamTableUtils;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.Row;
import org.apache.commons.csv.CSVFormat;

import java.io.Serializable;
import java.util.Iterator;


/**
 * IOReader for {@code BeamTextCSVTable}.
 */
public class BeamTextCSVTableIOReader extends PTransform<PCollection<String>, PCollection<Row>> implements Serializable {
	private static final long serialVersionUID = 6563991875822627899L;
	private Schema schema;
	private CSVFormat csvFormat;
	private ErrorHandler errorHandler;

	public BeamTextCSVTableIOReader(Schema schema, CSVFormat csvFormat, ErrorHandler h) {
		this.schema = schema;
		this.csvFormat = csvFormat;
		this.errorHandler = h;
	}

	@Override
	public PCollection<Row> expand(PCollection<String> input) {
		PCollection<Row> collection = input.apply(
				ParDo.of(
						new DoFn<String, Row>() {
							private static final long serialVersionUID = 301354818390875427L;

							@ProcessElement
							public void processElement(ProcessContext ctx) {
								String str = ctx.element();
								try {
									Iterator<Row> it =
											BeamTableUtils.csvLines2BeamRows(BeamTextCSVTableIOReader.this.csvFormat,
													str, BeamTextCSVTableIOReader.this.schema).iterator();
									while (it.hasNext()) {
										ctx.output(it.next());
									}
								} catch (Exception e) {
									BeamTextCSVTableIOReader.this.errorHandler.addError(e);
								}
							}

						}));
		return collection.setRowSchema(BeamTextCSVTableIOReader.this.schema);
	}


}

