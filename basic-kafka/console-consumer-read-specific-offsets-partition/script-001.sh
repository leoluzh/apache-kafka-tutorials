#!/bin/zsh
kafka-topics --create --topic example-topic --bootstrap-server broker:9092 --replication-factor 1 --partitions 2