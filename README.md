# Spark at Scale
 
This demo simulates a stream of movie ratings.  Data flows from akka -> kafka -> spark streaming -> cassandra

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
`java -Xmx1g -jar feeder/target/scala-2.10/feeder-assembly-1.0.jar 2>&1 1>feeder-out.log &`


## Run Spark Streaming

* build the streaming jar
`sbt streaming/package`

* running locally for development
`spark-submit --packages org.apache.spark:spark-streaming-kafka-assembly_2.10:1.4.1,com.datastax.spark:spark-cassandra-connector-java_2.10:1.4.0-M3 --class sparkAtScale.StreamingRatings streaming/target/scala-2.10/streaming_2.10-1.0.jar`
 
* running on the server for production mode
`nohup spark-submit --packages org.apache.spark:spark-streaming-kafka-assembly_2.10:1.4.1,com.datastax.spark:spark-cassandra-connector-java_2.10:1.4.0-M3 --class sparkAtScale.StreamingRatings streaming/target/scala-2.10/streaming_2.10-1.0.jar 2>&1 1>streaming-out.log &`
