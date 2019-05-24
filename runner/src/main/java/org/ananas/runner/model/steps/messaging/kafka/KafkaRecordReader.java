package org.ananas.runner.model.steps.messaging.kafka;

import java.io.Serializable;
import org.apache.beam.sdk.io.kafka.KafkaRecord;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.Row;

public class KafkaRecordReader
    extends PTransform<PCollection<KafkaRecord<byte[], Row>>, PCollection<Row>>
    implements Serializable {

  private static final long serialVersionUID = 580779634813510874L;

  public KafkaRecordReader() {}

  @Override
  public PCollection<Row> expand(PCollection<KafkaRecord<byte[], Row>> input) {
    return input.apply(
        ParDo.of(
            new DoFn<KafkaRecord<byte[], Row>, Row>() {
              private static final long serialVersionUID = 5095780632524124324L;

              @ProcessElement
              public void processElement(ProcessContext ctx) {
                KafkaRecord<byte[], Row> txt = ctx.element();
                if (txt.getKV() != null) {
                  ctx.output(txt.getKV().getValue());
                }
              }
            }));
  }
}
