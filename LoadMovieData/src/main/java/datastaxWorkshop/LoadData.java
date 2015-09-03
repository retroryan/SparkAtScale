package datastaxWorkshop;

import com.datastax.driver.core.*;

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
            movieDataLocation = args[0];
            System.out.println("movieDataLocation = " + movieDataLocation);
            hostname = args[1];
            System.out.println("hostname = " + hostname);
            new LoadData().loadAndSaveMovieData(hostname, movieDataLocation);
        }
        else {
            System.out.println("Error!  Specify movie data location!");
        }



    }

    private void loadAndSaveMovieData(String hostname, String movieDataLocation) throws IOException {
        try (Cluster clusterConn = connect(hostname)) {
            try (Session session = clusterConn.newSession()) {

                PreparedStatement statement = session.prepare(
                        "INSERT INTO movie_db.movies " +
                                "(movie_id, title, categories) " +
                                "VALUES (?, ?, ?);");

                readMovieData(movieDataLocation, movieData -> saveMovieData(statement, session, movieData));
            }
        }
    }

    public Cluster connect(String node) {
        Cluster cluster = Cluster.builder()
                .addContactPoint(node)
                .build();
        Metadata metadata = cluster.getMetadata();
        System.out.printf("Connected to cluster: %s\n",
                metadata.getClusterName());
        for (Host host : metadata.getAllHosts()) {
            System.out.printf("Datacenter: %s; Host: %s; Rack: %s\n",
                    host.getDatacenter(), host.getAddress(), host.getRack());
        }
        return cluster;
    }

    private void readMovieData(String filePath, Consumer<MovieData> movieDataConsumer) throws IOException {

        FileReader fileReader = new FileReader(filePath);
        BufferedReader reader = new BufferedReader(
                fileReader);

        String line = reader.readLine();
        while (line != null) {
            //save to Cassandra here
            line = reader.readLine();
            processMovieDataLine(movieDataConsumer, line);
        }

    }

    private void processMovieDataLine(Consumer<MovieData> movieDataConsumer, String line) {
        if (line != null && line.length() > 0) {
            String[] split = line.split("::");
            if (split.length == 3) {
                int id = Integer.parseInt(split[0]);
                String title = split[1];
                String[] categoryArray = split[2].split("\\|");
                Set<String> categories = null;
                if (categoryArray.length > 0)
                    categories = new HashSet<>(Arrays.asList(categoryArray));

                MovieData movieData = new MovieData(id, title, categories);
                movieDataConsumer.accept(movieData);
            }
        }
    }

    private void saveMovieData(PreparedStatement statement, Session session, MovieData movieData) {
        BoundStatement boundStatement = new BoundStatement(statement);
        session.execute(boundStatement.bind(
                movieData.getMovie_id(),
                movieData.getTitle(),
                movieData.getCategories()));
    }

}
