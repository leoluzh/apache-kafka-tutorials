package com.lambdasys.developer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;

public class TutorialDataGenerator {

    private final Properties properties;

    public TutorialDataGenerator(final Properties properties) {
        this.properties = properties;
    }

    public void generate() {

        this.properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        this.properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        try (final var producer = new KafkaProducer<String, String>(properties)) {

            final var topic = properties.getProperty("input.topic.name");
            final var messages = Arrays.asList("All", "streams", "lead", "to", "Confluent", "Go", "to", "Kafka", "Summit");

            messages.stream().forEach(messsage -> {
                producer.send(new ProducerRecord<>(topic, messsage),
                        (metadata, exception) -> {
                            if (Objects.nonNull(exception)) {
                                exception.printStackTrace(System.out);
                            } else {
                                System.out.printf("Produced record at offset %d to topic %s %n",
                                        metadata.offset(),
                                        metadata.topic());
                            }
                        });
            });

        }

    }

}
