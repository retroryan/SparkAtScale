package driverDemo;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import common.ConnectionUtil;
import common.Movies;

import java.io.IOException;

public class MappingDemo {

    public void viewMovieData(String hostname) throws IOException {
        try (Cluster clusterConn = ConnectionUtil.connect(hostname)) {
            try (Session session = clusterConn.newSession()) {
                MappingManager manager = new MappingManager(session);
                Mapper<Movies> moviesMapper = manager.mapper(Movies.class);
                Movies movieBrazil = moviesMapper.get(1199);
                System.out.println("movieBrazil = " + movieBrazil);

            }
        }
    }
}
