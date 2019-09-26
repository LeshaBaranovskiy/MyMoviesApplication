package com.example.mymovies;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mymovies.adapters.ReviewAdapter;
import com.example.mymovies.adapters.TrailerAdapter;
import com.example.mymovies.data.FavouriteMovie;
import com.example.mymovies.data.MainViewModel;
import com.example.mymovies.data.Movie;
import com.example.mymovies.data.Review;
import com.example.mymovies.data.Trailer;
import com.example.mymovies.utils.JSONUtils;
import com.example.mymovies.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    private ImageView imageViewBigPoster;
    private ImageView imageViewAddtoFavourite;
    private TextView textViewTitle;
    private TextView textViewOriginalTitle;
    private TextView textViewRating;
    private TextView textViewReleaseDate;
    private TextView textViewOverview;
    private TextView textViewNoReviews;

    private RecyclerView recyclerViewTrailers;
    private RecyclerView recyclerViewReviews;
    private ReviewAdapter reviewAdapter;
    private TrailerAdapter trailerAdapter;

    private int id;
    private Movie movie;
    private MainViewModel mainViewModel;
    private FavouriteMovie favouriteMovie;

    String lang;

    //Создаем меню(три точки справа сверху), шаблон меню берем
    //из R.menu.main_menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        imageViewBigPoster = findViewById(R.id.imageViewPosterBig);
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewOriginalTitle = findViewById(R.id.textViewOriginalTitle);
        textViewRating = findViewById(R.id.textViewRating);
        textViewReleaseDate = findViewById(R.id.textViewReleaseDate);
        textViewOverview = findViewById(R.id.textViewOverview);
        imageViewAddtoFavourite = findViewById(R.id.imageViewAddToFavourite);
        recyclerViewReviews = findViewById(R.id.recyclerViewReviews);
        recyclerViewTrailers = findViewById(R.id.recyclerViewTrailers);
        textViewNoReviews = findViewById(R.id.textViewNoReviews);

        lang = Locale.getDefault().getLanguage();

        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        //Получение данных из интента, установление их в ImageView и TextViews
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("id")) {
            id = intent.getIntExtra("id", 0);
            if (intent.hasExtra("isFavourite")) {
                movie = mainViewModel.getFavouriteMovieById(id);
            } else  {
                movie = mainViewModel.getMovieById(id);
            }
            Picasso.get().load(movie.getBigPosterPath()).placeholder(R.drawable.placeholder_large).into(imageViewBigPoster);
            textViewTitle.setText(movie.getTitle());
            textViewOriginalTitle.setText(movie.getOriginalTitle());
            textViewRating.setText(Double.toString(movie.getVoteAverage()));
            textViewReleaseDate.setText(movie.getReleaseDate());
            textViewOverview.setText(movie.getOverview());
        } else {
            finish();
        }
        setStar();

        //Адаптеры для трейлеров и отзывов
        reviewAdapter = new ReviewAdapter();
        trailerAdapter = new TrailerAdapter();

        //Открытие трейлера в ютубе
        trailerAdapter.setOnTrailerClickListener(new TrailerAdapter.onTrailerClickListener() {
            @Override
            public void onTrailerClick(String url) {
                Intent intentToTrailer = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intentToTrailer);
            }
        });

        //Установление шаблонов и адаптеров для RecyclerView
        recyclerViewReviews.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTrailers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTrailers.setAdapter(trailerAdapter);
        recyclerViewReviews.setAdapter(reviewAdapter);

        //Получение JSON для отзывов и трейлеров
        JSONObject jsonObjectTrailers = NetworkUtils.getJSONForVideos(movie.getId(), lang);
        JSONObject jsonObjectReviews = NetworkUtils.getJSONForReviews(movie.getId(), lang);

        //Выделение из полученых JSON файлов отзывов и видео, занесение их в массив
        ArrayList<Trailer> trailers = JSONUtils.getTrailersJSON(jsonObjectTrailers);
        ArrayList<Review> reviews = JSONUtils.getReviewsJSON(jsonObjectReviews);

        //Установка в адаптеры отзывов и трейлеров из массивов
        trailerAdapter.setTrailers(trailers);
        reviewAdapter.setReviews(reviews);
        if (reviews.size() == 0) {
            textViewNoReviews.setText(R.string.no_reviews);
        }
    }

    //Изменение звезды при удалении, добавлении в избранное
    private void setStar() {
        favouriteMovie = mainViewModel.getFavouriteMovieById(id);
        if (favouriteMovie == null) {
            imageViewAddtoFavourite.setImageResource(R.drawable.favourite_add_to);
        } else {
            imageViewAddtoFavourite.setImageResource(R.drawable.favourite_remove);
        }
    }

    //Добавление и удаление из БД избранных фильмов
    public void onClickChangeFavourite(View view) {
        if (favouriteMovie == null) {
            mainViewModel.insertFavouriteMovie(new FavouriteMovie(movie));
            Toast.makeText(this, "Добавлено в избранное", Toast.LENGTH_SHORT).show();
        } else {
            mainViewModel.deleteFavouriteMovie(new FavouriteMovie(movie));
            Toast.makeText(this, "Удалено из избранного", Toast.LENGTH_SHORT).show();
        }
        setStar();
    }
}
