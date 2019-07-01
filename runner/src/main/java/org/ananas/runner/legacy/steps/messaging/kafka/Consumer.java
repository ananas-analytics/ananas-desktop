package org.ananas.runner.legacy.steps.messaging.kafka;

import java.util.List;
import java.util.Properties;
import org.apache.kafka.clients.consumer.KafkaConsumer;

class Consumer {

  static org.apache.kafka.clients.consumer.Consumer<String, String> Consumer(
      List<String> topics, String bootrapServers) {
    Properties props = new Properties();
    props.put("bootstrap.servers", bootrapServers);
    props.put("group.id", "test");
    props.put("enable.auto.commit", "true");
    props.put("auto.commit.interval.ms", "1000");
    props.put("session.timeout.ms", "30000");
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
    consumer.subscribe(topics);
    return consumer;
  }
}
