package com.lambdasys.developer;

import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;


public class KafkaProducerApplication {

    private final Producer<String,String> producer;
    private final String outTopic;

    public KafkaProducerApplication(
            final Producer<String,String> producer,
            final String topic
    ){
        this.producer = producer;
        this.outTopic = topic;
    }

    public void producer( final String message){

        final var parts = message.split("-");
        final String key;
        final String value;

        if( parts.length > 1 ){
            key = parts[0];
            value = parts[1];
        }else{
            key = null;
            value = parts[0];
        }

        final var producerRecord = new ProducerRecord<String,String>(this.outTopic,key,value);

        this.producer.send(
                producerRecord ,
                ( recordMetadata , ex ) -> {
                    if( ex != null ){
                        // mark to resend?
                        ex.printStackTrace();
                    }else{
                        System.out.println(
                                System.out.format("key/value %s/%s \twritten to topic[partition] %s [%s] at offset %s ",
                                        key ,
                                        value ,
                                        recordMetadata.topic() ,
                                        recordMetadata.partition() ,
                                        recordMetadata.offset() )
                        );
                    }
                }
        );


    }

    public void shutdown(){
        this.producer.close();
    }

    public static Properties load(final String filename) throws IOException {
        final var props = new Properties();
        try(final var input = new FileInputStream(filename)) {
            props.load(input);
        }
        return props;
    }

    public static void main(String... args) throws Exception {

        if( args.length < 2 ){
            throw new IllegalArgumentException(
                    "This program takes two arguments: the path to an environment configuration file and" +
                            "the path to the file with records to send");
        }

        final var props = KafkaProducerApplication.load(args[0]);


        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        props.put(ProducerConfig.ACKS_CONFIG, "all");

        props.put(ProducerConfig.CLIENT_ID_CONFIG, "kafka-producer-application");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        final var topic = props.getProperty("output.topic.name");
        final var producer = new KafkaProducer<String,String>(props);
        final var producerApp = new KafkaProducerApplication(producer, topic);

        var filepath = args[1];

        try{

            var lines = Files.readAllLines(Paths.get(filepath));

            lines.stream()
                    .filter(StringUtils::isNotBlank)
                    .forEach(producerApp::producer);

            System.out.println(System.out.format("Offsets and timestamps committed in batch from %s",filepath));

        }catch (IOException ex){
            System.err.printf("Error reading file %s to %s %n",filepath,ex);
        }finally {
            producerApp.shutdown();
        }
    }

}