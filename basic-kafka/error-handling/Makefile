
start-kafka-server:
	docker-compose up -d

stop-kafka-server:
	docker-compose down -rmi all -v

start-broker-bash:
	docker-compose exec broker bash

build-application:
	./gradlew build

build-shadow-application:
	./gradlew shadowJar

run-aplication:
	java -jar

build-docker-image:
	./gradlew jibDockerBuilde --image=leoluzh/error-handling:0.0.1

run-docker-image:
	docker run -v $PWD/configuration/prod.properties:/config.properties leoluzh/error-handling:0.0.1 config.properties