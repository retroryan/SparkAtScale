package datastaxWorkshop;

import java.util.Set;

public class MovieData {

    int movie_id;
    String title;
    Set<String> categories;

    public MovieData(int movie_id, String title, Set<String> categories) {
        this.movie_id = movie_id;
        this.title = title;
        this.categories = categories;
    }

    public int getMovie_id() {
        return movie_id;
    }

    public void setMovie_id(int movie_id) {
        this.movie_id = movie_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Set<String> getCategories() {
        return categories;
    }

    public void setCategories(Set<String> categories) {
        this.categories = categories;
    }

    @Override
    public String toString() {
        return "MovieData{" +
                "movie_id=" + movie_id +
                ", title='" + title + '\'' +
                ", categories=" + categories +
                '}';
    }
}
