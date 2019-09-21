package com.example.mymovies.utils;

import com.example.mymovies.data.Movie;
import com.example.mymovies.data.Review;
import com.example.mymovies.data.Trailer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JSONUtils {
        private static final String KEY_RESULTS = "results";

        //Для отзывов
        private static final String KEY_AUTHOR = "author";
        private static final String KEY_CONTENT = "content";

        //Для видео
        private static final String KEY_KEY_OF_VIDEO = "key";
        private static final String KEY_NAME = "name";
        private static final String BASE_YOUTUBE_URL = "https://www.youtube.com/watch?v=%s";

        //Вся информация о фильме
        private static final String KEY_VOTE_COUNT = "vote_count";
        private static final String KEY_ID = "id";
        private static final String KEY_TITLE = "title";
        private static final String KEY_ORIGINAL_TITLE = "original_title";
        private static final String KEY_OVERVIEW = "overview";
        private static final String KEY_POSTER_PATH = "poster_path";
        private static final String KEY_BACKDROP_PATH = "backdrop_path";
        private static final String KEY_VOTE_AVERAGE = "vote_average";
        private static final String KEY_RELEASE_DATE = "release_date";

        private static final String POSTER_BASE_URL = "https://image.tmdb.org/t/p/";
        private static final String SMALL_POSTER_SIZE = "w185";
        private static final String BIG_POSTER_SIZE = "w780";


        public static ArrayList<Movie> getMoviesJSON(JSONObject jsonObject) {
            ArrayList<Movie> result = new ArrayList<>();
            if (jsonObject == null) {
                return result;
            }
            try {
                JSONArray jsonArray = jsonObject.getJSONArray(KEY_RESULTS);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject objectMovie = jsonArray.getJSONObject(i);
                    int id = objectMovie.getInt(KEY_ID);
                    int voteCount = objectMovie.getInt(KEY_VOTE_COUNT);
                    String title = objectMovie.getString(KEY_TITLE);
                    String originalTitle = objectMovie.getString(KEY_ORIGINAL_TITLE);
                    String overview = objectMovie.getString(KEY_OVERVIEW);
                    String posterPath = POSTER_BASE_URL + SMALL_POSTER_SIZE + objectMovie.getString(KEY_POSTER_PATH);
                    String bigPosterPath = POSTER_BASE_URL + BIG_POSTER_SIZE + objectMovie.getString(KEY_POSTER_PATH);
                    String backdropPath = objectMovie.getString(KEY_BACKDROP_PATH);
                    double voteAverage = objectMovie.getDouble(KEY_VOTE_AVERAGE);
                    String releaseDate = "2019-01-01";
                    if (objectMovie.has(KEY_RELEASE_DATE)) {
                        releaseDate = objectMovie.getString(KEY_RELEASE_DATE);
                    }
                    Movie movie = new Movie(id, voteCount, title, originalTitle, overview, posterPath, bigPosterPath, backdropPath, voteAverage, releaseDate);
                    result.add(movie);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result;
        }

        public static ArrayList<Review> getReviewsJSON(JSONObject jsonObject) {
            ArrayList<Review> result = new ArrayList<>();
            if (jsonObject == null) {
                return result;
            }
            try {
                JSONArray jsonArray = jsonObject.getJSONArray(KEY_RESULTS);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject objectReview = jsonArray.getJSONObject(i);
                    String author = objectReview.getString(KEY_AUTHOR);
                    String content = objectReview.getString(KEY_CONTENT);
                    Review review = new Review(author, content);
                    result.add(review);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result;
        }

        public static ArrayList<Trailer> getTrailersJSON(JSONObject jsonObject) {
            ArrayList<Trailer> result = new ArrayList<>();
            if (jsonObject == null) {
                return result;
            }
            try {
                JSONArray jsonArray = jsonObject.getJSONArray(KEY_RESULTS);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject objectTrailers = jsonArray.getJSONObject(i);
                    String key = objectTrailers.getString(KEY_KEY_OF_VIDEO);
                    String video = String.format(BASE_YOUTUBE_URL, key);
                    String name = objectTrailers.getString(KEY_NAME);
                    Trailer trailer = new Trailer(video, name);
                    result.add(trailer);
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
            return result;
        }


}
