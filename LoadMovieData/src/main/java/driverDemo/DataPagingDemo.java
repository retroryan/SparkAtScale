package driverDemo;

import com.datastax.driver.core.*;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import common.ConnectionUtil;
import common.Movies;

import java.io.IOException;
import java.util.Scanner;


public class DataPagingDemo {

    public void viewMovieData(String hostname) throws IOException {
        try (Cluster clusterConn = ConnectionUtil.connect(hostname)) {
            try (Session session = clusterConn.newSession()) {

                MappingManager manager = new MappingManager(session);
                Mapper<Movies> moviesMapper = manager.mapper(Movies.class);
                pageMovieData(session);
            }
        }
    }


    public void pageMovieData(Session session) throws IOException {
        Scanner scanIn = new Scanner(System.in);

        Statement statement = new SimpleStatement("select * from movie_db.movies").setFetchSize(20);
        ResultSet resultSet = session.execute(statement);

        while (true) {
            displayNextPage(resultSet);

            PagingState nextPage = resultSet.getExecutionInfo().getPagingState();
            if (nextPage == null) {
                System.out.println("Finished fetching movie data");
                break;
            }


            System.out.println("Hit enter for the next 20 rows or q to quit: ");
            String continueStr = scanIn.nextLine();

            if (continueStr.equalsIgnoreCase("q"))
                break;

            statement.setPagingState(nextPage);
            resultSet = session.execute(statement);
        }


        scanIn.close();
    }

    public void displayNextPage(ResultSet resultSet) {

        int remaining = resultSet.getAvailableWithoutFetching();
        System.out.println("remaining = " + remaining);

        for (Row row : resultSet) {
            // Process the row ...
            System.out.println(row);
            if (--remaining == 0) {
                break;
            }
        }
    }



}
