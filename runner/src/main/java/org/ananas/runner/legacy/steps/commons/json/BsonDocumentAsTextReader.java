package org.ananas.runner.legacy.steps.commons.json;

import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.Row;
import org.bson.Document;

public class BsonDocumentAsTextReader extends PTransform<PCollection<Document>, PCollection<Row>> {

  private static final long serialVersionUID = -6665406554798597832L;

  private Schema schema;

  public BsonDocumentAsTextReader(Schema schema) {
    this.schema = schema;
  }

  @Override
  public PCollection<Row> expand(PCollection<Document> input) {
    return input.apply(
        ParDo.of(
            new DoFn<Document, Row>() {
              private static final long serialVersionUID = -7364417564214230507L;

              @ProcessElement
              public void processElement(ProcessContext ctx) {
                Document doc = ctx.element();
                if (doc != null) {
                  Row o = doc2Row(doc);
                  ctx.output(o);
                }
              }
            }));
  }

  public Row doc2Row(Document doc) {
    return Row.withSchema(BsonDocumentAsTextReader.this.schema).addValue(doc.toJson()).build();
  }
}
