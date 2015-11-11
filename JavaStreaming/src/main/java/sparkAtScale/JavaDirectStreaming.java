package sparkAtScale;

import kafka.serializer.StringDecoder;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Based on the example at:
 * https://github.com/apache/spark/blob/master/examples/src/main/java/org/apache/spark/examples/streaming/JavaSqlNetworkWordCount.java
 */
public class JavaDirectStreaming {

    public static void main(String[] args) {

        if (args.length < 2) {
            System.out.println("First parameter is kafka broker\n");
            System.out.println("Second is a list of one or more kafka topics to consume from\n\n");
            System.exit(-1);
        }
        String brokers = args[0];
        String topics = args[1];


        // Create context with 1 second batch interval
        SparkConf sparkConf = new SparkConf().setAppName("JavaDirectKafkaWordCount");
        JavaStreamingContext javaStreamingContext = new JavaStreamingContext(sparkConf, Durations.seconds(1));


        HashSet<String> topicsSet = new HashSet<>(Arrays.asList(topics.split(",")));
        HashMap<String, String> kafkaParams = new HashMap<>();
        kafkaParams.put("metadata.broker.list", brokers);

        // Create direct kafka stream with brokers and topics
        JavaPairInputDStream<String, String> ratingsStream = KafkaUtils.createDirectStream(
                javaStreamingContext,
                String.class,
                String.class,
                StringDecoder.class,
                StringDecoder.class,
                kafkaParams,
                topicsSet
        );

        basicWordsMapAndSave(ratingsStream);

        javaStreamingContext.start();
        javaStreamingContext.awaitTermination();
    }


    private static void basicWordsMapAndSave(JavaPairInputDStream<String, String> rawRatingsStream) {

        JavaDStream<Rating> ratingsStream = rawRatingsStream.map(tuple2 -> {
            String ratingLine = tuple2._2();
            String[] parsedRating = ratingLine.split("::");
            Integer user_id = Integer.parseInt(parsedRating[0].trim());
            Integer movie_id = Integer.parseInt(parsedRating[1].trim());
            Float rating = Float.parseFloat(parsedRating[2].trim());
            Long batch_time = (new DateTime(Long.parseLong(parsedRating[3].trim()))).getMillis();
            return new Rating(user_id, movie_id, rating, batch_time);
        });

        // Convert RDDs of the words DStream to DataFrame and run SQL query
        ratingsStream.foreachRDD((rdd, time) -> {
            SQLContext sqlContext = JavaSQLContextSingleton.getInstance(rdd.context());
            DataFrame ratingsDataFrame = sqlContext.createDataFrame(rdd, Rating.class);

            Map<String, String> optionsMap = new HashMap<String, String>();
            optionsMap.put("keyspace", "movie_db");
            optionsMap.put("table", "rating_by_movie");

            ratingsDataFrame.write().format("org.apache.spark.sql.cassandra")
                    .mode(SaveMode.Append)
                    .options(optionsMap)
                    .save();

            ratingsDataFrame.show();
            return null;
        });

    }

}

class JavaSQLContextSingleton {
    static private transient SQLContext instance = null;

    static public SQLContext getInstance(SparkContext sparkContext) {
        if (instance == null) {
            instance = new SQLContext(sparkContext);
        }
        return instance;
    }
}
