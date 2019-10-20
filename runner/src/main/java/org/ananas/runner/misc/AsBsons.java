package org.ananas.runner.misc;

import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.Row;
import org.bson.Document;

public class AsBsons extends PTransform<PCollection<Row>, PCollection<Document>> {

  private static final long serialVersionUID = 7533250121784263411L;
  private Bsonifier bsonifier;

  public static AsBsons of() {
    AsBsons o = new AsBsons();
    o.bsonifier = Bsonifier.of();
    return o;
  }

  @Override
  public PCollection<Document> expand(PCollection<Row> input) {
    return (PCollection)
        input.apply(
            MapElements.via(
                new SimpleFunction<Row, Document>() {
                  private static final long serialVersionUID = 7190631934884825741L;

                  @Override
                  public Document apply(Row i) {
                    return AsBsons.this.bsonifier.valueOfRow(i);
                  }
                }));
  }
}
