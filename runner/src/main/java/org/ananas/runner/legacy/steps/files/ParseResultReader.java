package org.ananas.runner.legacy.steps.files;

import java.io.Serializable;
import org.ananas.runner.kernel.errors.ErrorHandler;
import org.ananas.runner.kernel.errors.ExceptionHandler;
import org.apache.beam.sdk.io.tika.ParseResult;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.Row;
import org.apache.tika.metadata.Metadata;

public class ParseResultReader extends PTransform<PCollection<ParseResult>, PCollection<Row>>
    implements Serializable {

  private static final long serialVersionUID = 7220894541854129861L;
  protected Schema schema;
  protected ErrorHandler errors;

  ParseResultReader(Schema schema, ErrorHandler errors) {
    this.schema = schema;
    this.errors = errors;
  }

  @Override
  public PCollection<Row> expand(PCollection<ParseResult> input) {
    return input.apply(
        ParDo.of(
            new DoFn<ParseResult, Row>() {
              private static final long serialVersionUID = -7924230589949482219L;

              @ProcessElement
              public void processElement(ProcessContext ctx) {
                ParseResult txt = ctx.element();
                if (txt.isSuccess()) {
                  ctx.output(
                      Row.withSchema(ParseResultReader.this.schema)
                          .addValue(txt.getContent())
                          .addValue(toJSON(txt.getMetadata()))
                          .build());
                } else {
                  ParseResultReader.this.errors.addError(
                      ExceptionHandler.ErrorCode.GENERAL, txt.getError().getMessage());
                  System.err.println(txt.getError().getMessage());
                }
              }
            }));
  }

  private static String toJSON(Metadata meta) {
    StringBuilder b = new StringBuilder();
    b.append("{");
    String[] names = meta.names();
    for (int i = 0; i < meta.size(); i++) {
      b.append('"');
      b.append(names[i]);
      b.append('"');
      b.append(':');
      b.append('"');
      b.append(meta.get(names[i]));
      b.append('"');
      if (i != meta.size() - 1) {
        b.append(',');
      }
    }
    b.append("}");
    return b.toString();
  }
}
