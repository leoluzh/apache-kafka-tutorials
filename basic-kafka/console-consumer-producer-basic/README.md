# 1 - Initialize the project
To get started, make a new directory anywhere you’d like for this project:

```shell
mkdir console-consumer-producer-basic && cd console-consumer-producer-basic
```

# 2 - Get Confluent Platform
Next, create the following docker-compose.yml file to obtain Confluent Platform.

```dockerfile
---
version: '2'

services:
zookeeper:
image: confluentinc/cp-zookeeper:6.1.0
hostname: zookeeper
container_name: zookeeper
ports:
- "2181:2181"
environment:
ZOOKEEPER_CLIENT_PORT: 2181
ZOOKEEPER_TICK_TIME: 2000

broker:
image: confluentinc/cp-kafka:6.1.0
hostname: broker
container_name: broker
depends_on:
- zookeeper
ports:
- "29092:29092"
environment:
KAFKA_BROKER_ID: 1
KAFKA_ZOOKEEPER_CONNECT: 'zookeeper:2181'
KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://broker:9092,PLAINTEXT_HOST://localhost:29092
KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
```
  
Now launch Confluent Platform by running:

```shell
docker-compose up -d
```
# 3 - Create a topic
Your first step is to create a topic to produce to and consume from. Use the following command to create the topic:

```shell
docker-compose exec broker kafka-topics --create --topic example-topic --bootstrap-server broker:9092 --replication-factor 1 --partitions 1
```

#4 - Start a console consumer
Next let’s open up a console consumer to read records sent to the topic you created in the previous step.

From the same terminal you used to create the topic above, run the following command to open a terminal on the broker container:

```shell
docker-compose exec broker bash
```

From within the terminal on the broker container, run this command to start a console consumer:

```shell
kafka-console-consumer --topic example-topic --bootstrap-server broker:9092
```

The consumer will start up and block waiting for records, you won’t see any output until after the next step.

#5 - Produce your first records
To produce your first record into Kafka, open another terminal window and run the following command to open a second shell on the broker container:

```shell
docker-compose exec broker bash
```

From inside the second terminal on the broker container, run the following command to start a console producer:

```shell
kafka-console-producer --topic example-topic --bootstrap-server broker:9092
```

The producer will start and wait for you to enter input. Each line represents one record and to send it you’ll hit the enter key. If you type multiple words and then hit enter, the entire line is considered one record.

Try typing one line at a time, hit enter and go back to the console consumer window and look for the output. Or, you can select all the records and send at one time.

```
the
lazy
fox
jumped over the brown cow
```

Once you’ve sent all the records you should see the same output in your console consumer window. After you’ve confirmed receiving all records, go ahead and close the consumer by entering <kbd>CTRL</kbd><kbd>+</kbd><kbd>C</kbd>.

#6 - Read all records
In the first consumer example, you observed all incoming records because the consumer was already running, waiting for incoming records.

But what if the records were produced before you started your consumer? In that case you wouldn’t see the records as the console consumer by default only reads incoming records arriving after it has started up.

But what about reading previously sent records? In that case, you’ll add one property ```--from-beginning``` to the start command for the console consumer.

To demonstrate reading from the beginning, close the current console consumer.

Now go back to your producer console and send the following records:

```
how now
brown cow
all streams lead
to Kafka!
```

#7 - Start a new consumer to read all records
Next, let’s open up a console consumer again. This time you’ll read everything your producer has sent to the topic you created in the previous step.

Run this command in the container shell you created for your first consumer and note the additional property ```--from-beginning```:

```shell
kafka-console-consumer --topic example-topic --bootstrap-server broker:9092  --from-beginning
```

After the consumer starts you should see the following output in a few seconds:

```
the
lazy
fox
jumped over the brown cow
how now
brown cow
all streams lead
to Kafka!
```

One word of caution with using the --from-beginning flag. As the name implies this setting forces the consumer retrieve every record currently on the topic. So it’s best to use when testing and learning and not on a production topic.

Again, once you’ve received all records, close this console consumer by entering a <kbd>CTRL</kbd><kbd>+</kbd><kbd>C</kbd>.

# 8 - Produce records with full key-value pairs
Kafka works with key-value pairs, but so far you’ve only sent records with values only. Well to be fair you’ve sent key-value pairs, but the keys are null. Sometimes you’ll need to send a valid key in addition to the value from the command line.

To enable sending full key-value pairs from the command line you add two properties to your console producer, parse.key and key.separator

Let’s try to send some full key-value records now. If your previous console producer is still running close it with a <kbd>CTRL</kbd><kbd>+</kbd><kbd>C</kbd> and run the following command to start a new console producer:

```shell
kafka-console-producer --topic example-topic --bootstrap-server broker:9092 \
--property parse.key=true \
--property key.separator=":"
```

Then enter these records either one at time or copy-paste all of them into the terminal and hit enter:

```
key1:what a lovely
key1:bunch of coconuts
foo:bar
fun:not quarantine
```

# 9 - Start a consumer to show full key-value pairs
Now that we’ve produced full key-value pairs from the command line, you’ll want to consume full key-value pairs from the command line as well.

If your console consumer from the previous step is still open, shut it down with a <kbd>CTRL</kbd><kbd>+</kbd><kbd>C</kbd>. Then run the following command to re-open the console consumer but now it will print the full key-value pair. Note the added properties of print.key and key.separator. You should also take note that there’s a different key separator used here, you don’t have to use the same one between console producers and consumers.

```shell
kafka-console-consumer --topic example-topic --bootstrap-server broker:9092 \
--from-beginning \
--property print.key=true \
--property key.separator="-"
```

After the consumer starts you should see the following output in a few seconds:

```
null-the
null-lazy
null-fox
null-jumped over the brown cow
null-how now
null-brown cow
null-all streams lead
null-to Kafka!
key1-what a lovely
key1-bunch of coconuts
foo-bar
fun-not quarantine
```

Since we kept the ```--from-beginning``` property, you’ll see all the records sent to the topic. You’ll notice the results before you sent keys are formatted as null-<value>.

# 10 - Clean Up
You’re all done now!

Go back to your open windows and stop any console producers and consumers with a <kbd>CTRL</kbd><kbd>+</kbd><kbd>C</kbd> then close the container shells with a <kbd>CTRL</kbd><kbd>+</kbd><kbd>D</kbd> command.

Then you can shut down the stack by running:

```shell
docker-compose down
```