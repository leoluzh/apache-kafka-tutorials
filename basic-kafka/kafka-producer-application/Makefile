
start-kafka-server:
	docker-compose up -d

stop-kafka-server:
	docker-compose down -rmi all -v

start-broker-bash:
	docker-compose exec broker bash

create-app-jar:
	./gradlew shadowJar

run-app:
	java -jar build/libs/kafka-producer-application-standalone-0.0.1.jar configuration/dev.properties input.txt

build-docker-image:
	./gradlew jibDockerBuild --image=leoluzh/kafka-producer-application:0.0.1

test-app:
	./gradlew test

launch-container:
	docker run -v $PWD/configuration/prod.properties:/config.properties leoluzh/kafka-producer-application:0.0.1 config.properties