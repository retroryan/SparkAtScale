package sparkAtScale;

public class Rating {

    Integer user_id;
    Integer movie_id;
    Float rating;
    Long timestamp;

    public Rating(Integer user_id, Integer movie_id, Float rating, Long batch_time) {
        this.user_id = user_id;
        this.movie_id = movie_id;
        this.rating = rating;
        this.timestamp = batch_time;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public Integer getMovie_id() {
        return movie_id;
    }

    public void setMovie_id(Integer movie_id) {
        this.movie_id = movie_id;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
