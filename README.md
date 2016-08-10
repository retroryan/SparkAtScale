# Spark at Scale

This demo simulates a stream of movie ratings.  Data flows from akka -> kafka -> spark streaming -> cassandra

# Setting up SBT

http://www.scala-sbt.org/0.13/tutorial/Installing-sbt-on-Linux.html

## Kafka Setup

[See the Kafka Setup Instructions in the KAFKA_SETUP.md file](KAFKA_SETUP.md)

## Download and load the movielens data

* Download the movielens 10 million ratings data set from http://grouplens.org/datasets/movielens/

* copy the movie data (movies.data and ratings.data) into the data directory

* [Follow the setup instructions in the LoadMovieData / MOVIE_DATA_README.md](LoadMovieData/MOVIE_DATA_README.md)

* Readme also has an option demo of making the movie lens data searchable with Solr

## Setup Akka Feeder

* build the feeder fat jar   
`sbt feeder/assembly`

* run the feeder

Copy the application.conf file to dev.conf and modify the zookeeper location.  Then override the configs by using -Dconfig.file=dev.conf to use the new config.

`java -Xmx1g -Dconfig.file=dev.conf -jar feeder/target/scala-2.10/feeder-assembly-1.0.jar 1 100 true 2>&1 1>feeder-out.log &`



## Run Spark Streaming

* build the streaming jar
`sbt streaming/package`

* copy the jar from target to server -  i.e. the jar at streaming/target/scala-2.10/streaming_2.10-0.1.jar

 * running on a server in foreground
 
first parameter is kafka broker and the second parameter whether to display debug output  (true|false)

`dse spark-submit --packages org.apache.spark:spark-streaming-kafka_2.10:1.6.1 --class sparkAtScale.StreamingDirectRatings streaming_2.10-0.1.jar 10.0.0.4:9092 ratings true`

* running on the server for production mode

`nohup dse spark-submit --conf spark.driver.host=10.0.0.4 --packages org.apache.spark:spark-streaming-kafka_2.10:1.6.1 --class sparkAtScale.StreamingDirectRatings streaming_2.10-0.1.jar  10.0.0.4:9092 ratings true 2>&1 1>streaming-out.log &`

* if you see an error with spark host failed to connet try setting:

`--conf spark.driver.host=10.0.0.4`


## Spark Notebook

`screen -m -d -S "snb" bash -c 'bin/spark-notebook -Dhttp.port=9042 >> notebook.out'`