package com.lambdasys.developer;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.streams.KeyValue;

import org.junit.Test;

public class KafkaProducerApplicationTest {

    private final static String TEST_CONFIG_FILE = "configuration/test.properties" ;

    @Test
    public void testProduce() throws IOException {

        final var stringSerializer = new StringSerializer();
        final var mockProducer = new MockProducer<String,String>(true,stringSerializer,stringSerializer);
        final var properties = KafkaProducerApplication.loadProperties(TEST_CONFIG_FILE);
        final var topic = properties.getProperty("output.topic.name");
        final var producerApplication = new KafkaProducerApplication(mockProducer,topic);

        final var records = Arrays.asList(
                "foo-bar",
                "bar-foo",
                "baz-bar",
                "great:weather");

        records.stream().forEach(producerApplication::produce);

        final var expected = Arrays.asList(
                KeyValue.pair("foo","bar") ,
                KeyValue.pair("bar","foo") ,
                KeyValue.pair("baz","bar") ,
                KeyValue.pair(null,"great:weather")
        );

        final var actual = mockProducer
                .history()
                .stream()
                .map(this::toKeyValue)
                .toList();

        assertThat(actual, equalTo(expected));
        producerApplication.shutdown();

    }

    private KeyValue<String,String> toKeyValue(final ProducerRecord<String,String> producerRecord){
        return KeyValue.pair(producerRecord.key(),producerRecord.value());
    }


}
