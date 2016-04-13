package datastaxWorkshop;

import com.datastax.driver.core.*;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import common.ConnectionUtil;
import common.Movies;
import common.Rating;
import driverDemo.DataPagingDemo;
import driverDemo.MappingDemo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class LoadData {

    public static void main(String[] args) throws IOException {

        String movieDataLocation = "";
        String hostname = "";
        if (args.length > 1) {

            String loadOrView = args[0];
            if (loadOrView.equalsIgnoreCase("load")) {
                movieDataLocation = args[1];
                System.out.println("movieDataLocation = " + movieDataLocation);
                hostname = args[2];
                System.out.println("hostname = " + hostname);
                new LoadData().loadAndSaveMovieData(hostname, movieDataLocation);
            }
            else  if (loadOrView.equalsIgnoreCase("view")){
                hostname = args[1];
                System.out.println("hostname = " + hostname);
                new DataPagingDemo().viewMovieData(hostname);
            }
            else {
                hostname = args[1];
                System.out.println("hostname = " + hostname);
                new MappingDemo().viewMovieData(hostname);
            }
        }
        else {
            System.out.println("Error!  Specify load or view along with movie data location for loading!");
        }



    }

    private void loadAndSaveMovieData(String hostname, String movieDataLocation) throws IOException {
        try (Cluster clusterConn = ConnectionUtil.connect(hostname)) {
            try (Session session = clusterConn.newSession()) {

                MappingManager manager = new MappingManager(session);
                Mapper<Movies> moviesMapper = manager.mapper(Movies.class);
                //readMovieData(movieDataLocation, movieData -> saveMovieWithMapper(moviesMapper, movieData));

                PreparedStatement insertRatingStatement = session.prepare(
                        "INSERT INTO movie_db.rating_by_movie " +
                                "(user_id, movie_id, rating, timestamp) " +
                                "VALUES (?, ?, ?, ?);");

                readRatingData(movieDataLocation, ratingData -> saveRatingData(insertRatingStatement, session, ratingData));

            }
        }
    }


    private void readMovieData(String filePath, Consumer<Movies> movieDataConsumer) throws IOException {

        FileReader fileReader = new FileReader(filePath + "/movies.dat");
        BufferedReader reader = new BufferedReader(
                fileReader);

        String line = reader.readLine();
        while (line != null) {
            //save to Cassandra here
            line = reader.readLine();
            processMovieDataLine(movieDataConsumer, line);
        }

    }

    private void processMovieDataLine(Consumer<Movies> movieDataConsumer, String line) {
        if (line != null && line.length() > 0) {
            String[] split = line.split("::");
            if (split.length == 3) {
                int id = Integer.parseInt(split[0]);
                String title = split[1];
                String[] categoryArray = split[2].split("\\|");
                Set<String> categories = null;
                if (categoryArray.length > 0)
                    categories = new HashSet<>(Arrays.asList(categoryArray));

                Movies movies = new Movies(id, title, categories);
                movieDataConsumer.accept(movies);
            }
        }
    }

    private void saveMovieWithMapper(Mapper<Movies> moviesMapper, Movies movie) {
        moviesMapper.save(movie);
    }

    private void readRatingData(String filePath, Consumer<Rating> ratingDataConsumer) throws IOException {
        FileReader fileReader = new FileReader(filePath + "/ratings.dat");
        BufferedReader reader = new BufferedReader(
                fileReader);

        String line = reader.readLine();
        while (line != null) {
            //save to Cassandra here
            line = reader.readLine();
            processRatingDataLine(ratingDataConsumer, line);
        }

    }

    private void processRatingDataLine(Consumer<Rating> ratingDataConsumer, String line) {
        if (line != null && line.length() > 0) {
            String[] split = line.split("::");
            if (split.length == 4) {
                int user_id = Integer.parseInt(split[0]);
                int movie_id = Integer.parseInt(split[1]);
                float raw_rating = Float.parseFloat(split[2]);
                int timestamp = Integer.parseInt(split[3]);

                Rating rating = new Rating(movie_id, user_id, raw_rating, timestamp);
                ratingDataConsumer.accept(rating);
            }
        }
    }

    private void saveRatingData(PreparedStatement insertRatingStatement, Session session, Rating rating) {
        BoundStatement boundStatement = new BoundStatement(insertRatingStatement);
        session.execute(boundStatement.bind(
               rating.getUser_id(),
                rating.getMovie_id(),
                rating.getRating(),
                rating.getTimestamp()));
    }

}
