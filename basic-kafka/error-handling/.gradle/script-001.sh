#!/bin/zsh
docker-compose exec broker kafka-console-consumer --bootstrap-server broker:9092 --topic out-topic --from-beginning --max-messages 12