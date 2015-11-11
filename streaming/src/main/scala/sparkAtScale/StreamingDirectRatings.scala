package sparkAtScale

import java.util.Arrays

import kafka.serializer.StringDecoder
import org.apache.hadoop.conf.Configuration
import org.apache.spark.deploy.SparkHadoopUtil
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{SQLContext, SaveMode}
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Milliseconds, StreamingContext, Time}
import org.joda.time.DateTime

/** This uses the Kafka Direct introduced in Spark 1.4
  *
  */
object StreamingDirectRatings {

  def main(args: Array[String]) {

    if (args.length < 3) {
      println("first parameter is kafka broker ")
      println("second parameter is the kafka topic")
      println("third param whether to display debug output  (true|false) ")
    }

    val brokers = args(0)
    val topicsArg = args(1)
    val debugOutput = args(2).toBoolean

    val conf = new SparkConf()
    val sc = SparkContext.getOrCreate(conf)

    def createStreamingContext(): StreamingContext = {
      @transient val newSsc = new StreamingContext(sc, Milliseconds(1000))
      println(s"Creating new StreamingContext $newSsc")
      newSsc
    }


    val ssc = StreamingContext.getActiveOrCreate(createStreamingContext)

    val sqlContext = SQLContext.getOrCreate(sc)
    import sqlContext.implicits._

    val topics: Set[String]= topicsArg.split(",").map(_.trim).toSet

    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers)
    println(s"connecting to brokers: $brokers")
    println(s"ssc: $ssc")
    println(s"kafkaParams: $kafkaParams")
    println(s"topics: $topics")

    val ratingsStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)

    ratingsStream.foreachRDD {
      (message: RDD[(String, String)], batchTime: Time) => {

        message.isEmpty()

        // convert each RDD from the batch into a Ratings DataFrame
        //rating data has the format user_id:movie_id:rating:timestamp
        val df = message.map {
          case (key, nxtRating) =>
            val parsedRating = nxtRating.split("::")
            val timestamp: Long = new DateTime(parsedRating(3).trim.toLong).getMillis
            Rating(parsedRating(0).trim.toInt, parsedRating(1).trim.toInt, parsedRating(2).trim.toFloat, timestamp)
        }.toDF()

        // this can be used to debug dataframes
        if (debugOutput)
          df.show()

        // save the DataFrame to Cassandra
        // Note:  Cassandra has been initialized through dse spark-submit, so we don't have to explicitly set the connection
        df.write.format("org.apache.spark.sql.cassandra")
          .mode(SaveMode.Append)
          .options(Map("keyspace" -> "movie_db", "table" -> "rating_by_movie"))
          .save()
      }
    }

    ssc.start()
    ssc.awaitTermination()
  }
}
