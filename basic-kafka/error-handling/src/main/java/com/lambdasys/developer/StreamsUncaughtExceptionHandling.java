package com.lambdasys.developer;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

public class StreamsUncaughtExceptionHandling {

    private int counter = 0;

    public Topology buildTopology(final Properties properties) {

        final var builder = new StreamsBuilder();
        final var inputTopic = properties.getProperty("input.topic.name");
        final var outputTopic = properties.getProperty("output.topic.name");

        builder.stream(inputTopic, Consumed.with(Serdes.String(), Serdes.String()))
                .mapValues(value -> {
                    counter++;
                    if (counter == 2 || counter == 8 || counter == 15) {
                        throw new IllegalStateException("it works on my box!!!");
                    }
                    return value.toUpperCase();
                });

        return builder.build();

    }

    public void createTopics(final Properties properties){
        try(AdminClient client = AdminClient.create(properties)){

            final var topics = new ArrayList<NewTopic>();
            final var sessionInput = new NewTopic(
                    properties.getProperty("input.topic.name"),
                    Integer.parseInt(properties.getProperty("input.topic.partitions")),
                    Short.parseShort(properties.getProperty("input.topic.replication.factor")));

            topics.add(sessionInput);

            final  var sessionOutput = new NewTopic(
                    properties.getProperty("output.topic.name") ,
                    Integer.parseInt(properties.getProperty("output.topic.partitions")),
                    Short.parseShort(properties.getProperty("output.topic.replication.factor"))
            );

            topics.add(sessionOutput);
            client.createTopics(topics);

        }
    }

    public Properties loadEnvironmentProperties(final String filename) throws IOException {
        final var properties = new Properties();
        final var input = new FileInputStream(filename);
        properties.load(input);
        input.close();
        return properties;
    }

    public static void main(String... args) throws Exception {

        if( args.length < 1 ){
            throw new IllegalArgumentException("This program takes one argument: the path to an environment configuration file.");
        }

        final var application = new StreamsUncaughtExceptionHandling();
        final var properties = application.loadEnvironmentProperties(args[0]);

        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,Serdes.String().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,Serdes.String().getClass());
        // Change this to StreamsConfig.EXACTLY_ONCE to eliminate duplicates
        properties.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG,StreamsConfig.AT_LEAST_ONCE);

        final var topology = application.buildTopology(properties);
        final var dataGenerator = new TutorialDataGenerator(properties);
        dataGenerator.generate();

        final var maxFailures = Integer.parseInt(properties.getProperty("max.failures"));
        final var maxTimeInterval = Long.parseLong(properties.getProperty("max.time.millis"));

        final var streams = new KafkaStreams(topology,properties);

        final var exceptionHandler = new MaxFailuresUncaughtExceptionHandler(
                maxFailures,
                maxTimeInterval);

        streams.setUncaughtExceptionHandler(exceptionHandler);

        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook"){
            @Override
            public void run(){
                streams.close();
            }
        });

        try{
            streams.cleanUp();
            streams.start();
        }catch (Throwable t){
            System.exit(1);
        }

    }

}
