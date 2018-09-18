package com.epam.bdcc.workshop.common.kafka;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * Created by Dmitrii_Kober on 3/15/2018.
 */
public class KafkaSender {


    @FunctionalInterface
    public interface OnSuccess {
        void apply(RecordMetadata metadata, String payload);
    }

    @FunctionalInterface
    public interface OnError {
        void apply(String topic, String payload, Exception e);
    }

    private static final Logger LOG = LoggerFactory.getLogger(KafkaSender.class);

    private final Producer<String, String> producer;
    private final OnSuccess onSuccess;
    private final OnError onError;


    public KafkaSender(KafkaClientFactory kafkaClientFactory) {
        this(
                kafkaClientFactory,
                (metadata, payload) -> { LOG.info("Sent payload='{" + payload + "}' to topic='{" + metadata.topic() + "}'"); },
                (topic, payload, e) -> { LOG.error("An error occurred while sending a message {" + payload + "} to topic='{" + topic + "}", e); }
        );
    }

    public KafkaSender(KafkaClientFactory kafkaClientFactory, OnSuccess onSuccess, OnError onError) {
        this.producer = kafkaClientFactory.producer();
        this.onSuccess = onSuccess;
        this.onError = onError;
    }

    public void send(String topic, String payload) {
        producer.send(
                new ProducerRecord<>(topic, UUID.randomUUID().toString(), payload),
                (recordMetadata, e) -> {
                    if (null != e) { onError.apply(topic, payload, e); }
                    else { onSuccess.apply(recordMetadata, payload); }
                }
        );
    }

    public void close() {
        if (producer != null) {
            producer.close();
        }
    }
}
