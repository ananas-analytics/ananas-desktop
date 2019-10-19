package org.ananas.runner.steprunner.files.txt;

import java.io.Serializable;
import org.ananas.runner.core.errors.ErrorHandler;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.Row;

public class TextReader extends PTransform<PCollection<String>, PCollection<Row>>
    implements Serializable {

  private static final long serialVersionUID = 7228634251297025816L;
  protected Schema schema;

  public TextReader(Schema schema, ErrorHandler errors) {
    super();
    this.schema = schema;
  }

  @Override
  public PCollection<Row> expand(PCollection<String> input) {
    return input.apply(
        ParDo.of(
            new DoFn<String, Row>() {
              private static final long serialVersionUID = -1378045159224729816L;

              @ProcessElement
              public void processElement(ProcessContext ctx) {
                String txt = ctx.element();
                ctx.output(Row.withSchema(TextReader.this.schema).addValue(txt).build());
              }
            }));
  }
}
