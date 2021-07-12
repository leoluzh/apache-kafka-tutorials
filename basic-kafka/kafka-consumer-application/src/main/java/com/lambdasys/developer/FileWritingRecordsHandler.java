package com.lambdasys.developer;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;

public class FileWritingRecordsHandler implements ConsumerRecordsHandler<String,String> {

    private final Path path;

    public FileWritingRecordsHandler(final Path path) {
        this.path = path;
    }

    public void process(final ConsumerRecords<String,String> consumerRecords) {
        final var values = new ArrayList<String>();
        consumerRecords.forEach( record -> {
            values.add(record.value());
        });
        if(CollectionUtils.isNotEmpty(values)){
            try{
                Files.write(path,values, StandardOpenOption.CREATE,StandardOpenOption.WRITE,StandardOpenOption.APPEND);
            }catch (IOException ex){
                throw new RuntimeException(ex);
            }
        }
    }
}