package com.example.mymovies.utils;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;


//Класс для получения данных в виде JSON
public class NetworkUtils {

    private static final String BASE_URL = "https://api.themoviedb.org/3/discover/movie";
    private static final String BASE_URL_VIDEOS = "https://api.themoviedb.org/3/movie/%s/videos";
    private static final String BASE_URL_REVIEWS = "https://api.themoviedb.org/3/movie/%s/reviews";

    //Параметры в URL
    private static final String PARAMS_API_KEY = "api_key";
    private static final String PARAMS_LANGUAGE = "language";
    private static final String PARAMS_SORT_BY = "sort_by";
    private static final String PARAMS_PAGE = "page";
    private static final String PARAMS_MINIMUM_VOTE_COUNT = "vote_count.gte";

    //Значения параметров
    private static final String API_KEY = "8a77017c71c8b4aa83a35641b8d725b8";
    private static final String SORT_BY_POPULARITY = "popularity.desc";
    private static final String SORT_BY_TOP_RATED = "vote_average.desc";
    private static final String MINIMUM_VOTE_COUNT = "700";

    public static final int POPULARITY = 0;
    public static final int TOP_RATED = 1;

    //Построение URL
    public static URL buildURLToVideos(int movieId, String lang) {
        URL result = null;
        String urlVideos = String.format(BASE_URL_VIDEOS, movieId);

        //Построение url, buildUpon == "?", appendQueryParameter == "&"
        Uri uri = Uri.parse(urlVideos).buildUpon().
                appendQueryParameter(PARAMS_API_KEY, API_KEY).
                appendQueryParameter(PARAMS_LANGUAGE, lang).build();
        try {
            result = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return result;
    }

    //Построение URL
    public static URL buildURLToReviews(int movieId, String lang) {
        URL result = null;
        String urlReviews = String.format(BASE_URL_REVIEWS, movieId);
        Uri uri = Uri.parse(urlReviews).buildUpon().
                appendQueryParameter(PARAMS_API_KEY, API_KEY).
                appendQueryParameter(PARAMS_LANGUAGE, lang).build();
        try {
            result = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return result;
    }

    //Построение URL
    public static URL buildURL(int sortBy, int page, String lang) {
        URL result = null;
        String methodOfSort;
        if (sortBy == POPULARITY) {
            methodOfSort = SORT_BY_POPULARITY;
        } else {
            methodOfSort = SORT_BY_TOP_RATED;
        }
        Uri uri = Uri.parse(BASE_URL).buildUpon().
                appendQueryParameter(PARAMS_API_KEY, API_KEY).
                appendQueryParameter(PARAMS_LANGUAGE, lang).
                appendQueryParameter(PARAMS_SORT_BY, methodOfSort).
                appendQueryParameter(PARAMS_MINIMUM_VOTE_COUNT, MINIMUM_VOTE_COUNT).
                appendQueryParameter(PARAMS_PAGE, Integer.toString(page)).build();
        try {
            result = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return result;
    }


    //Получение данных в виде JSON из URL которые мы построили выше
    public static JSONObject getJSONFromNetwork(int sortBy, int page, String lang) {
        URL url = buildURL(sortBy, page, lang);
        JSONObject result = null;
        try {
            result = new JSONLoadTask().execute(url).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    //Получение данных в виде JSON из URL которые мы построили выше
    public static JSONObject getJSONForVideos(int id, String lang) {
        URL url = buildURLToVideos(id, lang);
        JSONObject result = null;
        try {
            result = new JSONLoadTask().execute(url).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    //Получение данных в виде JSON из URL которые мы построили выше
    public static JSONObject getJSONForReviews(int id, String lang) {
        URL url = buildURLToReviews(id, lang);
        JSONObject result = null;
        try {
            result = new JSONLoadTask().execute(url).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    //Класс который выполняет подключение к заданому URL, принимает значение
    //типа URL, а возвращает значение типа JSON
    private static class JSONLoadTask extends AsyncTask<URL, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(URL... urls) {
            JSONObject result = null;
            if (urls == null || urls.length == 0) {
                return result;
            }
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) urls[0].openConnection();
                InputStream in = connection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);
                BufferedReader reader = new BufferedReader(inputStreamReader);
                StringBuilder  builder = new StringBuilder();
                String line = reader.readLine();
                while (line != null) {
                    builder.append(line);
                    line = reader.readLine();
                }
                result = new JSONObject(builder.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return result;
        }
    }

    //Лоадер, чтобы при медленном интеренете не крашнулось приложение
    public static class JSONLoader extends AsyncTaskLoader<JSONObject> {

        private Bundle bundle;
        private OnStartLoadingListener onStartLoadingListener;

        public void setOnStartLoadingListener(OnStartLoadingListener onStartLoadingListener) {
            this.onStartLoadingListener = onStartLoadingListener;
        }

        public interface OnStartLoadingListener {
            void onStartLoading();
        }

        public JSONLoader(@NonNull Context context, Bundle bundle) {
            super(context);
            this.bundle = bundle;
        }

        //Чтобы при инициализации происходила загрузка, обязательно
        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            if (onStartLoadingListener != null) {
                onStartLoadingListener.onStartLoading();
            }
            forceLoad();
        }

        @Nullable
        @Override
        public JSONObject loadInBackground() {
            if (bundle == null) {
                return null;
            }
            String urlAsString = bundle.getString("url");
            URL url = null;
            try {
                url = new URL(urlAsString);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            JSONObject result = null;
            if (url == null) {
                return result;
            }
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) url.openConnection();
                InputStream in = connection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);
                BufferedReader reader = new BufferedReader(inputStreamReader);
                StringBuilder  builder = new StringBuilder();
                String line = reader.readLine();
                while (line != null) {
                    builder.append(line);
                    line = reader.readLine();
                }
                result = new JSONObject(builder.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return result;
        }
    }
}
