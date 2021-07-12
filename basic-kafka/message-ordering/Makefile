

start-kafka-server:
	docker-compose up -d

create-kafka-topic:
	docker-compose exec broker kafka-topics --bootstrap-server broker:9092 --topic my-topic --create --replication-factor 1 --partitions 2

describe-kafka-topic:
	docker-compose exec broker kafka-topics --bootstrap-server broker:9092 --topic my-topic --describe

stop-kafka-server:
	docker-compose down -rmi all -v

create-app-jar:
	./gradlew shadowJar

run-app:
	java -jar build/libs/message-ordering-standalone-0.0.1.jar configuration/dev.properties input.txt

run-producer-app:
	java -jar build/libs/kafka-producer-application-0.0.1.jar configuration/dev.properties input.txt

run-kafka-consumer-topic:
	docker-compose exec broker kafka-console-consumer --topic my-topic --bootstrap-server broker:9092 --from-beginning --property print.key=true --property key.separator=" : "

run-kafka-consumer-topic-partition-zero:
	docker-compose exec broker kafka-console-consumer --bootstrap-server localhost:9092 --topic my-topic --property print.key=true --property key.separator=, --partition 0 --from-beginning

run-kafka-consumer-topic-partition-one:
	docker-compose exec broker kafka-console-consumer --bootstrap-server localhost:9092 --topic my-topic --property print.key=true --property key.separator=, --partition 1 --from-beginning

run-kafka-consumer-dump-log-partition-zero:
	docker-compose exec broker kafka-dump-log --print-data-log --files '/var/lib/kafka/data/my-topic-0/00000000000000000000.log' --deep-iteration

run-kafka-consumer-dump-log-partition-one:
	docker-compose exec broker kafka-dump-log --print-data-log --files '/var/lib/kafka/data/my-topic-1/00000000000000000000.log' --deep-iteration