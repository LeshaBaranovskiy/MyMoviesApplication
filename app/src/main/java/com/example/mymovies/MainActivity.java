package com.example.mymovies;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.example.mymovies.adapters.MovieAdapter;
import com.example.mymovies.data.MainViewModel;
import com.example.mymovies.data.Movie;
import com.example.mymovies.utils.JSONUtils;
import com.example.mymovies.utils.NetworkUtils;

import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//Интерфейс LoaderManager.LoaderCallbacks<JSONObject> используется,
//чтобы показать что данная активность является слушателем
public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<JSONObject> {

    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private Switch switchSort;
    private ProgressBar loading;

    private MainViewModel viewModel;

    private static final int LOADER_ID = 133;
    private static int methodOfSort;
    private static int page = 1;

    private LoaderManager loaderManager;
    private static boolean isLoading = false;

    private static String lang;

    //Создаем меню(три точки справа сверху), шаблон меню берем
    //из R.menu.main_menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Создание
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);

        //Прячем кнопку перехода в MainActivity при нахождении в MainActivity
        MenuItem mainItem = menu.findItem(R.id.itemMain);
        mainItem.setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    //Переход в другую активность при нажатии на пункт меню
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.itemMain:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.itemFavourite:
                Intent intentToFavourite = new Intent(this, FavouriteActivity.class);
                startActivity(intentToFavourite);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //Один из классов интерфейса, который нужно переопределить
    //Создает загрузчик
    @NonNull
    @Override
    public Loader<JSONObject> onCreateLoader(int id, @Nullable Bundle args) {
        NetworkUtils.JSONLoader jsonLoader = new NetworkUtils.JSONLoader(this, args);
        jsonLoader.setOnStartLoadingListener(new NetworkUtils.JSONLoader.OnStartLoadingListener() {
            @Override
            public void onStartLoading() {
                loading.setVisibility(View.VISIBLE);
                isLoading = true;
            }
        });
        return jsonLoader;
    }

    //Один из классов интерфейса, который нужно переопределить
    @Override
    public void onLoadFinished(@NonNull Loader<JSONObject> loader, JSONObject data) {
        ArrayList<Movie> movies = JSONUtils.getMoviesJSON(data);
        if (movies != null && !movies.isEmpty()) {
            if (page == 1) {
                viewModel.deleteAllMovies();
                movieAdapter.clear();
            }
            for(Movie movie: movies) {
                viewModel.insertMovie(movie);
            }
            movieAdapter.addMovies(movies);
            page++;
        }
        loading.setVisibility(View.INVISIBLE);
        isLoading = false;
        loaderManager.destroyLoader(LOADER_ID);
    }

    //Один из классов интерфейса, который нужно определить
    @Override
    public void onLoaderReset(@NonNull Loader<JSONObject> loader) {

    }

    public int getColumnCount() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int) (displayMetrics.widthPixels / displayMetrics.density);
        return width/185 > 2 ? width/185 : 2;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loaderManager = LoaderManager.getInstance(this);

        recyclerView = findViewById(R.id.recyclerViewPosters);
        switchSort = findViewById(R.id.switchSort);
        loading = findViewById(R.id.progressBarLoading);

        lang = Locale.getDefault().getLanguage();

        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        //Адаптер
        recyclerView.setLayoutManager(new GridLayoutManager(this, getColumnCount()));
        movieAdapter = new MovieAdapter();
        recyclerView.setAdapter(movieAdapter);

        //Переключение между категориями фильмов
        //Метод setChecked вызывается 2 раза для первой имитации включения и выключения
        //переключателя и подгрузки фильмов
        switchSort.setChecked(true);
        switchSort.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                page = 1;
                setMethodOfSort(b);
            }
        });
        switchSort.setChecked(false);

        //Действие при нажатии на фильм
        movieAdapter.setOnPosterClickListener(new MovieAdapter.onPosterClickListener() {
            @Override
            public void onPosterClick(int position) {
                Movie movie = movieAdapter.getMovies().get(position);
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("id", movie.getId());
                startActivity(intent);
            }
        });

        //Подгрузка фильмов, когда доходим до 4-го с конца элемента
        movieAdapter.setOnReachEndListener(new MovieAdapter.onReachEndListener() {
            @Override
            public void onReachEnd() {
            if (!isLoading) {
                downLoadData(methodOfSort, page);
            }
            }
        });

        //Заносим в массив данные из БД
        LiveData<List<Movie>> moviesFromLiveData = viewModel.getMovies();

        moviesFromLiveData.observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(List<Movie> movies) {
                if (page == 1) {
                    movieAdapter.setMovies(movies);
                }
            }
        });
    }

    //Переключение переключателя switchSort
    public void onClickSetPopularity(View view) {
        setMethodOfSort(false);
        switchSort.setChecked(false);
    }
    //Переключение переключателя switchSort
    public void onClickSetTopRated(View view) {
        setMethodOfSort(true);
        switchSort.setChecked(true);
    }

    //Установка метода сортировки в зависимости от положение switchSort
    private void setMethodOfSort(boolean isTopRated) {

        if (isTopRated) {
            methodOfSort = NetworkUtils.TOP_RATED;
        } else  {
            methodOfSort = NetworkUtils.POPULARITY;
        }
        downLoadData(methodOfSort, page);
    }

    //Заносим URL в Bundle и запускаем лоадер
    private void downLoadData(int methodOfSort, int page) {
        URL url = NetworkUtils.buildURL(methodOfSort, page, lang);
        Bundle bundle = new Bundle();
        bundle.putString("url", url.toString());
        loaderManager.restartLoader(LOADER_ID, bundle, this);
    }
}














