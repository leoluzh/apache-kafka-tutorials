#!/bin/zsh
kafka-topics --create --topics output-topic --bootstrap-server broker:9092 --replication-factor 1 --partitions 1