package sparkAtScale

case class Rating(user_id: Int, movie_id: Int, rating: Float, timestamp:Long)

case class AverageRating(movie_id: Int, average_rating: Float, timestamp:Long)
