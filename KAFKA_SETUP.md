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

nohup bin/zookeeper-server-start.sh config/zookeeper.properties  2>&1 1>runlogs/zookeeper.log &
 
nohup bin/kafka-server-start.sh config/server.properties 2>&1 1>runlogs/kafka.log &

#kafka 0.8
bin/kafka-create-topic.sh --zookeeper localhost:2181 --replica 1 --partition 1 --topic ratings
bin/kafka-list-topic.sh --zookeeper localhost:2181

#kafka 0.8.2
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 5 --topic ratings
bin/kafka-topics.sh --list --zookeeper localhost:2181

bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic ratings --from-beginning


bin/kafka-topics.sh --zookeeper localhost:2181 --delete --topic ratings