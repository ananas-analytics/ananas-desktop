package org.ananas.runner.legacy.steps.messaging.kafka;

import com.google.common.base.Verify;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.ananas.runner.kernel.AbstractStepRunner;
import org.ananas.runner.kernel.StepRunner;
import org.ananas.runner.kernel.common.JsonStringBasedFlattenerReader;
import org.ananas.runner.kernel.errors.ErrorHandler;
import org.ananas.runner.kernel.model.StepType;
import org.ananas.runner.kernel.schema.JsonAutodetect;
import org.ananas.runner.kernel.schema.SchemaBasedRowConverter;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaConnector extends AbstractStepRunner implements StepRunner, Serializable {

  private static final Logger LOG = LoggerFactory.getLogger(KafkaConnector.class);

  private static final String SCHEMA = "schemas";
  private static final int MAX_BUFFER_SIZE = 10;
  private static final long serialVersionUID = 5496100959235578829L;

  public KafkaConnector(
      Pipeline pipeline,
      String stepId,
      String bootstrapServers,
      List<String> topics,
      String consumerGroupId) {
    super(StepType.Connector);
    Schema schema = autodetect(bootstrapServers, topics, false);
    LOG.debug("Schema : " + schema);
    if (schema.getFields().isEmpty()) {
      throw new RuntimeException(
          "Can't autodetect schemas because the topics are empty. Please retry later");
    }
    Map<String, Object> props = new HashMap<>();
    props.put("group.id", consumerGroupId);
    props.put(SCHEMA, schema);
    this.stepId = stepId;
    this.output =
        pipeline
            .apply(
                KafkaIO.<byte[], Row>read()
                    .withKeyDeserializer(ByteArrayDeserializer.class)
                    .withValueDeserializer(RowDeserializer.class)
                    .withBootstrapServers(bootstrapServers)
                    .withTopics(topics)
                    .withReadCommitted()
                    .withMaxNumRecords(2L)
                    .updateConsumerProperties(props))
            .apply(new KafkaRecordReader());

    this.output.setRowSchema(schema);
  }

  public static class RowDeserializer implements Deserializer<Row>, Serializable {

    private static final long serialVersionUID = -6371502674683593832L;
    JsonStringBasedFlattenerReader reader;

    public RowDeserializer() {
      this.reader = null;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
      Schema schema = (Schema) configs.get(SCHEMA);
      Verify.verifyNotNull(schema);
      this.reader =
          new JsonStringBasedFlattenerReader(
              SchemaBasedRowConverter.of(schema), new ErrorHandler());
    }

    @Override
    public Row deserialize(String topic, byte[] data) {
      return this.reader.document2BeamRow(new String(data));
    }

    @Override
    public void close() {}
  }

  private static Schema autodetect(
      String bootstrapServers, List<String> topics, boolean parseString) {
    org.apache.kafka.clients.consumer.Consumer<String, String> consumer =
        Consumer.Consumer(topics, bootstrapServers);
    List<String> buffer = new LinkedList<>();
    // usually the stream application would be running forever,
    // for autodetecting purpose we just let it test for some time and stop since the input data
    // need to be finite.
    int i = 15;
    while (i-- > 0) {
      try {
        Thread.sleep(1000L);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      for (ConsumerRecord<String, String> record : consumer.poll(250)) {
        // System.out.printf("offset = %d, key = %s, value = %s", record.offset(), record.key(),
        // record.value());
        buffer.add(record.value());
        if (buffer.size() > MAX_BUFFER_SIZE) {
          break;
        }
      }
    }
    consumer.close();
    return JsonAutodetect.autodetectJson(buffer.iterator(), parseString, DEFAULT_LIMIT);
  }
}
