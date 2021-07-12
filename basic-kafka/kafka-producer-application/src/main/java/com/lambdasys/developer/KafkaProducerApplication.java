package com.lambdasys.developer;

import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class KafkaProducerApplication {

    private final Producer<String,String> producer;
    private String outTopic;


    public KafkaProducerApplication( final Producer<String,String> producer ,
                                     final String topic ){
        this.producer = producer ;
        this.outTopic = topic ;
    }

    public Future<RecordMetadata> produce( final String message ){

        final var parts = message.split("-");
        final var key = parts.length > 1 ? parts[0] : "no-key" ;
        final var value = parts.length > 1 ? parts[1] : parts[0] ;

        final var producerRecord = new ProducerRecord<String,String>(outTopic,key,value);
        return producer.send( producerRecord );

    }

    public void shutdown(){
        producer.close();
    }

    public static Properties loadProperties( final String filename ) throws IOException {
        final var envProperties = new Properties();
        final var input = new FileInputStream(filename);
        envProperties.load(input);
        input.close();
        return envProperties;
    }

    public void printMetadata(final Collection<Future<RecordMetadata>> metadata,
                              final String filename ){
        System.out.printf("Offsets and timestamps committed in batch from %s",filename);
        metadata.stream().forEach( m -> {
            try{
                final var recordMetadata = m.get();
                System.out.printf(
                        "Record written to offset %s timestamp %s in partition %s",
                        recordMetadata.offset(),
                        recordMetadata.timestamp(),
                        recordMetadata.partition());
            }catch(InterruptedException | ExecutionException ex){
                if( ex instanceof InterruptedException ){
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    public static void main(String... args) throws Exception {

        if( args.length < 2 ){
            throw new IllegalArgumentException(
                    "This program takes two arguments: " +
                            "the path to an environment configuration file and " +
                            "the path to the file with records to send."
            );
        }

        final var props = KafkaProducerApplication.loadProperties(args[0]);
        final var topic = props.getProperty("output.topic.name");
        final var producer = new KafkaProducer<String,String>(props);
        final var producerApplication = new KafkaProducerApplication(producer,topic);

        final var filepath = args[1];

        try{
            final var lines = Files.readAllLines(Paths.get(filepath));
            final var metadata = lines.stream()
                    .filter(StringUtils::isNotBlank)
                    .map(producerApplication::produce)
                    .toList();
            producerApplication.printMetadata(metadata,filepath);
        }catch(IOException ex){
            System.err.printf("Error reading file %s due to %s %n",filepath,ex);
        }finally {
            producerApplication.shutdown();
        }

    }

}