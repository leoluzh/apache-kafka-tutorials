package com.lambdasys.developer;

import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class MaxFailuresUncaughtExceptionHandler implements StreamsUncaughtExceptionHandler {

    private final int maxFailures;
    private final long maxTimeIntervalMillis;
    private Instant previousErrorTime;
    private int currentFailureCount;

    public MaxFailuresUncaughtExceptionHandler(final int maxFailures, final long maxTimeIntervalMillis){
        this.maxFailures = maxFailures;
        this.maxTimeIntervalMillis = maxTimeIntervalMillis;
    }

    @Override
    public StreamThreadExceptionResponse handle(final Throwable throwable) {
        this.currentFailureCount++;
        final var currentErrorTime = Instant.now();

        if(Objects.isNull(this.previousErrorTime)){
            previousErrorTime = currentErrorTime;
        }

        final var millisBetweenFailure = ChronoUnit.MILLIS.between(previousErrorTime,currentErrorTime);

        if(currentFailureCount >= maxFailures ){
            if(millisBetweenFailure <= maxTimeIntervalMillis){
                return StreamThreadExceptionResponse.SHUTDOWN_APPLICATION;
            }else{
                this.currentFailureCount = 0 ;
                this.previousErrorTime = null ;
            }
        }

        return StreamThreadExceptionResponse.REPLACE_THREAD;

    }

}
