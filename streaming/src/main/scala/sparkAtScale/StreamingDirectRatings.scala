package sparkAtScale

import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Milliseconds, Seconds, StreamingContext, Time}
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.apache.spark.SparkConf
import kafka.serializer.StringDecoder
import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.Row
import org.apache.spark.rdd.RDD
import org.joda.time.DateTime

/** This uses the Kafka Direct introduced in Spark 1.4
  *
  */
object StreamingDirectRatings {

  val CHECKPOINT_PATH = "/ratingsCP"

  def main(args: Array[String]) {

    if (args.length < 3) {
      println("first paramteter is kafka broker ")
      println("second param whether to display debug output  (true|false) ")
      println("third param whether to enable check pointing (true|false) ")
    }

    val brokers = args(0)
    val debugOutput = args(1).toBoolean
    val checkpointOn = args(2).toBoolean

    val conf = new SparkConf()

    val sc = SparkContext.getOrCreate(conf)

    def createStreamingContext(): StreamingContext = {
      @transient val newSsc = new StreamingContext(sc, Milliseconds(500))
      println(s"Creating new StreamingContext $newSsc")

      if (checkpointOn) {
        println(s"checkpoint set to: $CHECKPOINT_PATH")
        newSsc.checkpoint(CHECKPOINT_PATH)
      }

      newSsc
    }

    val ssc = if (checkpointOn)
      StreamingContext.getActiveOrCreate(CHECKPOINT_PATH, createStreamingContext)
    else
      StreamingContext.getActiveOrCreate(createStreamingContext)

    val sqlContext = SQLContext.getOrCreate(sc)
    import sqlContext.implicits._

    val topics = Set("ratings")
    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers)
    println(s"connecting to brokers: $brokers")

    val ratingsStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)

    ratingsStream.foreachRDD {
      (message: RDD[(String, String)], batchTime: Time) => {
        // convert each RDD from the batch into a Ratings DataFrame
        //rating data has the format user_id:movie_id:rating:timestamp
        val df = message.map {
          case (key, nxtRating) => nxtRating.split("::")
        }.map(rating => {
          val timestamp: Long = new DateTime(rating(3).trim.toLong).getMillis
          Rating(rating(0).trim.toInt, rating(1).trim.toInt, rating(2).trim.toFloat, timestamp)
        } ).toDF("user_id", "movie_id", "rating", "timestamp")

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
