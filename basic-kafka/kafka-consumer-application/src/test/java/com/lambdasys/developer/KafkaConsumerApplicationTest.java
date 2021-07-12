package com.lambdasys.developer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.TopicPartition;
import org.junit.Test;

import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;

public class KafkaConsumerApplicationTest {

    private final static String TEST_CONFIG_FILE = "configuration/test.properties";

    @Test
    public void consumerTest() throws Exception {

        final var tempFilePath = Files.createTempFile("test-consumer-output", ".out");
        final var recordsHandler = new FileWritingRecordsHandler(tempFilePath);
        final var testConsumerProps = KafkaConsumerApplication.loadProperties(TEST_CONFIG_FILE);
        final var topic = testConsumerProps.getProperty("input.topic.name");
        final var topicPartition = new TopicPartition(topic, 0);
        final var mockConsumer = new MockConsumer<String, String>(OffsetResetStrategy.EARLIEST);
        final var consumerApplication = new KafkaConsumerApplication(mockConsumer, recordsHandler);

        final var worker = new Thread(() -> consumerApplication.runConsume(testConsumerProps));
        worker.start();
        Thread.sleep(1000);
        addTopicPartitionsAssigmentAdnAddConsumerRecords(
                topic,
                mockConsumer,
                topicPartition);
        Thread.sleep(1000);
    }

    private void addTopicPartitionsAssigmentAdnAddConsumerRecords(final String topic,
                                                                  final MockConsumer<String, String> mockConsumer,
                                                                  final TopicPartition topicPartition) {
        final var beginningOffsets = new HashMap<TopicPartition, Long>();
        beginningOffsets.put(topicPartition, 0L);
        mockConsumer.rebalance(Collections.singletonList(topicPartition));
        mockConsumer.updateBeginningOffsets(beginningOffsets);
        addConsumerRecords(mockConsumer, topic);
    }

    private void addConsumerRecords(final MockConsumer<String, String> mockConsumer,
                                    final String topic) {
        mockConsumer.addRecord(new ConsumerRecord<>(topic, 0, 0, null, "foo"));
        mockConsumer.addRecord(new ConsumerRecord<>(topic, 0, 1, null, "bar"));
        mockConsumer.addRecord(new ConsumerRecord<>(topic, 0, 2, null, "baz"));
    }

}
