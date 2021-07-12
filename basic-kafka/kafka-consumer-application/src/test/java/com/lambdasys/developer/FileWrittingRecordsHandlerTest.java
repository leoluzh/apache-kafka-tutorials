package com.lambdasys.developer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class FileWrittingRecordsHandlerTest {

    public void testProcess () throws IOException {

        final var tempFilePath = Files.createTempFile("test-handler",".out");

        try{
            final var recordsHandler = new FileWritingRecordsHandler(tempFilePath);
            recordsHandler.process(createConsumerRecords());
            final var expectedWords = Arrays.asList("it's but", "a flesh wound", "come back");
            List<String> actualRecords = Files.readAllLines(tempFilePath);
            assertThat(actualRecords, equalTo(expectedWords));
        }finally {
            Files.deleteIfExists(tempFilePath);
        }

    }

    private ConsumerRecords<String,String> createConsumerRecords(){
        final var topic = "test";
        final var partition = 0;
        final var topicPartition = new TopicPartition(topic, partition);
        final var consumerRecordsList = new ArrayList<ConsumerRecord<String, String>>();
        consumerRecordsList.add(new ConsumerRecord<>(topic, partition, 0, null, "it's but"));
        consumerRecordsList.add(new ConsumerRecord<>(topic, partition, 0, null, "a flesh wound"));
        consumerRecordsList.add(new ConsumerRecord<>(topic, partition, 0, null, "come back"));
        final var recordsMap = new HashMap<TopicPartition, List<ConsumerRecord<String, String>>>();
        recordsMap.put(topicPartition, consumerRecordsList);
        return new ConsumerRecords<>(recordsMap);
    }

}
