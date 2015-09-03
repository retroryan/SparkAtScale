Kafka Setup - Local Machine
==============================

Start Kafka, create the topics and test:

bin/zookeeper-server-start.sh config/zookeeper.properties

bin/kafka-server-start.sh config/server.properties

bin/kafka-create-topic.sh --zookeeper localhost:2181 --replica 1 --partition 1 --topic ratings

bin/kafka-list-topic.sh --zookeeper localhost:2181

bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic ratings --from-beginning

Kafka Server Setup - Remote Server Setup
===================================

Everything is run in the kafka root directory:

mkdir runlogs

nohup bin/zookeeper-server-start.sh config/zookeeper.properties > runlogs/zookeeper.log 2> runlogs/zookeeper.err < /dev/null &

nohup bin/kafka-server-start.sh config/server.properties > runlogs/kafka.log 2> runlogs/kafka.err < /dev/null &

bin/kafka-create-topic.sh --zookeeper localhost:2181 --replica 1 --partition 1 --topic ratings

bin/kafka-list-topic.sh --zookeeper localhost:2181

bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic ratings --from-beginning