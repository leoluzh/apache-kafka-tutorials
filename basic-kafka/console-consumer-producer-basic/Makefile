

start-kafka-server:
	docker-compose up -d

create-kafka-topic:
	docker-compose exec broker kafka-topics --create --topic example-topic --bootstrap-server broker:9092 --replication-factor 1 --partitions 1

start-kafka-consumer:
	docker-compose exec broker bash

start-kafka-producer:
	docker-compose exec broker bash

stop-kafka-server:
	docker-compose down -rmi all -v