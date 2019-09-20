package com.example.mymovies;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mymovies.data.FavouriteMovie;
import com.example.mymovies.data.MainViewModel;
import com.example.mymovies.data.Movie;
import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    private ImageView imageViewBigPoster;
    private ImageView imageViewAddtoFavourite;
    private TextView textViewTitle;
    private TextView textViewOriginalTitle;
    private TextView textViewRating;
    private TextView textViewReleaseDate;
    private TextView textViewOverview;

    private int id;
    private Movie movie;
    private MainViewModel mainViewModel;
    private FavouriteMovie favouriteMovie;

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

        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("id")) {
            id = intent.getIntExtra("id", 0);
            movie = mainViewModel.getMovieById(id);
            Picasso.get().load(movie.getBigPosterPath()).into(imageViewBigPoster);
            textViewTitle.setText(movie.getTitle());
            textViewOriginalTitle.setText(movie.getOriginalTitle());
            textViewRating.setText(Double.toString(movie.getVoteAverage()));
            textViewReleaseDate.setText(movie.getReleaseDate());
            textViewOverview.setText(movie.getOverview());
        } else {
            finish();
        }
        setStar();
    }

    private void setStar() {
        favouriteMovie = mainViewModel.getFavouriteMovieById(id);
        if (favouriteMovie == null) {
            imageViewAddtoFavourite.setImageResource(R.drawable.favourite_add_to);
        } else {
            imageViewAddtoFavourite.setImageResource(R.drawable.favourite_remove);
        }
    }

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
