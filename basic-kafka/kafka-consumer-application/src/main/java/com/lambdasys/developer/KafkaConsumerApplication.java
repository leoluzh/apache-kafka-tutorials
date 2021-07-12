package com.lambdasys.developer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

public class KafkaConsumerApplication {

    private volatile boolean keepConsuming = true;
    private Consumer<String,String> consumer;
    private ConsumerRecordsHandler<String,String> recordsHandler;
    
    public KafkaConsumerApplication( final Consumer<String,String> consumer , 
                                     final ConsumerRecordsHandler<String,String> recordsHandler){
        this.consumer = consumer;
        this.recordsHandler = recordsHandler;
    }
    
    public void runConsume(final Properties consumerProperties){
        try{
            this.consumer.subscribe(Collections.singletonList(consumerProperties));
            while (this.keepConsuming){
                final var consumerRecords = consumer.poll(Duration.ofSeconds(1));
                this.recordsHandler.process(consumerRecords);
            }
        }finally {
            consumer.close();
        }
    }

    public void shutdown(){
        this.keepConsuming = false;
    }

    public static Properties loadProperties(final String filename) throws IOException {
        var props = new Properties();
        var input = new FileInputStream(filename);
        props.load(input);
        return props;
    }

    public static void main(String... args) throws Exception {

        if( args.length < 1 ){
            throw new IllegalArgumentException(
                    "This program takes one argument: the path to an environment configuration file."
            );
        }

        final var consumerProperties = KafkaConsumerApplication.loadProperties(args[0]);
        final var filename = consumerProperties.getProperty("file.path");
        final var consumer = new KafkaConsumer<String,String>(consumerProperties);
        final var recordsHandler = new FileWritingRecordsHandler(Paths.get(filename));
        final var consumerApplication = new KafkaConsumerApplication(consumer,recordsHandler);

        Runtime.getRuntime().addShutdownHook(new Thread(consumerApplication::shutdown));
        consumerApplication.runConsume(consumerProperties);

    }

}