CREATE KEYSPACE IF NOT EXISTS movie_db WITH replication = {'class':'NetworkTopologyStrategy', 'DC1':3};

create table movie_db.movies (
    movie_id int primary key,
    title text,
    categories set<text>
);

create table movie_db.rating_by_movie (
    movie_id int,
    user_id int,
    rating float,
    timestamp int,
    primary key(movie_id, user_id)
);

create table movie_db.average_rating (
    movie_id int,
    average_rating float,
    timestamp bigint,
    primary key(movie_id, timestamp)
);

create table movie_db.movies_by_category (
    category text,
    movie_id int,
    title text,
    primary key(category, movie_id)
);