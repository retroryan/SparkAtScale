package sparkAtScale

import kafka.serializer.StringDecoder
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{SQLContext, SaveMode}
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, Milliseconds, StreamingContext, Time}
import org.joda.time.DateTime

/** This uses the Kafka Direct introduced in Spark 1.4
  * In addition to writing to C* it also joins the DF with the Movie Lens Data
  *
  */
object StreamJoinDirectRatings {
  def main(args: Array[String]) {

    val conf = new SparkConf()

    val sc = SparkContext.getOrCreate(conf)

    def createStreamingContext(): StreamingContext = {
      @transient val newSsc = new StreamingContext(sc, Seconds(1))
      println(s"Creating new StreamingContext $newSsc")

      newSsc
    }
    val ssc = StreamingContext.getActiveOrCreate(createStreamingContext)
    //ssc.checkpoint("/ratingsCP")

    val sqlContext = SQLContext.getOrCreate(sc)
    import sqlContext.implicits._

    if (args.length < 2) {
      print("Supply the kafka broker as the first parameter and whether to display debug output as the second parameter (true|false) ")
    }

    val moviesDF = sqlContext.read.format("org.apache.spark.sql.cassandra")
      .options(Map("keyspace" -> "movie_db", "table" -> "movies"))
      .load()
      .cache()

    moviesDF.filter(moviesDF("movie_id") === "51678").show()

    val brokers = args(0)
    val topics = Set("ratings")
    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers)
    println(s"connecting to brokers: $brokers")
    val debugOutput = args(1).toBoolean

    val ratingsStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)


    ratingsStream.foreachRDD {
      (message: RDD[(String, String)], batchTime: Time) => {
        // convert each RDD from the batch into a Ratings DataFrame
        //rating data has the format user_id:movie_id:rating:timestamp
        val ratingsDF = message.map {
          case (key, nxtRating) => nxtRating.split("::")
        }.map(rating => {
          val timestamp: Long = new DateTime(rating(3).trim.toLong).getMillis
          Rating(rating(0).trim.toInt, rating(1).trim.toInt, rating(2).trim.toFloat, timestamp)
        } ).toDF("user_id", "rating_movie_id", "rating", "timestamp")



        //CREATE TABLE IF NOT EXISTS movie_db.movies_with_ratings (rating float, user_id int, movie_id int, categories text, title text, timestamp bigint, PRIMARY KEY(movie_id, userid));

        val joinedMovieRatingDF = moviesDF.join(ratingsDF, moviesDF("movie_id") === ratingsDF("rating_movie_id"))
          .drop("rating_movie_id")
          .toDF("movie_id", "categories", "title", "user_id", "rating", "timestamp")

        // this can be used to debug dataframes
        if (debugOutput)
          joinedMovieRatingDF.show()

        // save the DataFrame to Cassandra
        // Note:  Cassandra has been initialized through dse spark-submit, so we don't have to explicitly set the connection
        joinedMovieRatingDF.write.format("org.apache.spark.sql.cassandra")
          .mode(SaveMode.Append)
          .options(Map("keyspace" -> "movie_db", "table" -> "movies_with_ratings"))
          .save()
      }
    }

    ssc.start()
    ssc.awaitTermination()
  }
}
