package com.example.android.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class DetailActivity extends AppCompatActivity implements LoaderCallbacks<Movie>, DetailActivityInterface {
    private static final String BASE_MOVIE_URL = "https://api.themoviedb.org/3/movie/";
    private static final String URL_VIDEOS = "/videos";
    private static final String URL_REVIEWS = "/reviews";
    private static final String URL_API_KEY = "?api_key=";
    private static String movieDbApiKey;
    private static final String KEY_MOVIE = "movie";
    private Movie movie;
    private static final int LOADER_ID = 1;
    private TextView Title;
    private ImageView Poster;
    private TextView ReleaseDate;
    private TextView VoteAverage;
    private TextView Overview;
    private ImageButton Favorite;
    private VideoAdapter videoAdapter;
    private ReviewAdapter reviewAdapter;
    private RecyclerView Videos;
    private RecyclerView Reviews;
    private static final String BASE_VIDEO_URL = "https://www.youtube.com/embed/";
    private static final String KEY_VIDEO_VISIBILITY = "video_visibility";
    private static final String KEY_REVIEW_VISIBILITY = "review_visibility";
    private static final String BASE_POSTER_URL = "https://image.tmdb.org/t/p/w185";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Title = findViewById(R.id.title);
        Poster = findViewById(R.id.poster);
        ReleaseDate = findViewById(R.id.release_date);
        VoteAverage = findViewById(R.id.vote_average);
        Favorite = findViewById(R.id.favorite);
        Overview = findViewById(R.id.overview_item);
        Videos = findViewById(R.id.video_list);
        Reviews = findViewById(R.id.review_list);
        if (savedInstanceState == null) {
            Videos.setVisibility(View.VISIBLE);
            Reviews.setVisibility(View.INVISIBLE);
        } else {
            Videos.setVisibility(savedInstanceState.getInt(KEY_VIDEO_VISIBILITY));
            Reviews.setVisibility(savedInstanceState.getInt(KEY_REVIEW_VISIBILITY));
        }

        ImageButton videoButton = findViewById(R.id.video_button);
        ImageButton reviewButton = findViewById(R.id.review_button);
        Intent intent = getIntent();
        movieDbApiKey = BuildConfig.MOVIE_DB_API_KEY;
        movie = intent.getParcelableExtra(KEY_MOVIE);
        Videos.setLayoutManager(new LinearLayoutManager(this));
        videoAdapter = new VideoAdapter(this);
        Videos.setAdapter(videoAdapter);
        Reviews.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter();
        Reviews.setAdapter(reviewAdapter);
        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);


        Favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageButton favorite = v.findViewById(R.id.favorite);
                if (movie.getIsFavorite()) {
                    movie.setIsFavorite(false);
                    String selection = MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=?";
                    String[] selectionArgs = new String[] {String.valueOf(movie.getId())};
                    getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI,
                            selection,
                            selectionArgs);
                    favorite.setImageResource(R.drawable.ic_favorite_border);
                } else {
                    movie.setIsFavorite(true);
                    ContentValues values = new ContentValues();
                    values.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movie.getId());
                    values.put(MovieContract.MovieEntry.COLUMN_TITLE, movie.getTitle());
                    Bitmap poster = movie.getPoster();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    poster.compress(Bitmap.CompressFormat.PNG, 0, stream);
                    byte[] posterArray = stream.toByteArray();
                    values.put(MovieContract.MovieEntry.COLUMN_POSTER, posterArray);
                    values.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
                    values.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, movie.getVoteAverage());
                    values.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, movie.getOverview());
                    getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI,
                            values);
                    favorite.setImageResource(R.drawable.ic_favorite);
                }
            }
        });

        videoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Videos.setVisibility(View.VISIBLE);
                Reviews.setVisibility(View.INVISIBLE);
            }
        });

        reviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Videos.setVisibility(View.INVISIBLE);
                Reviews.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_VIDEO_VISIBILITY, Videos.getVisibility());
        outState.putInt(KEY_REVIEW_VISIBILITY, Reviews.getVisibility());
        super.onSaveInstanceState(outState);
    }

    @NonNull
    @Override
    public Loader<Movie> onCreateLoader(int id, @Nullable Bundle args) {
        switch (id) {
            case LOADER_ID:
                return new MovieLoader(this, movie);
            default:
                throw new RuntimeException("Loader ID " + id + " not implemented");
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Movie> loader, Movie data) {
        movie = data;
        Title.setText(movie.getTitle());
        Bitmap poster = movie.getPoster();
        if (poster == null) Poster.setImageResource(R.drawable.popcorn_rendered);
        else {
            Poster.setImageBitmap(poster);
        }
        String date = movie.getReleaseDate();
        if (date.length() > 4) date = date.substring(0, 4);
        else date = "";
        ReleaseDate.setText(date);
        String voteAverage = "";
        String average = movie.getVoteAverage();
        if (!average.isEmpty()) voteAverage = getString(R.string.vote_average, average);
        VoteAverage.setText(voteAverage);
        String overview = movie.getOverview();
        if (overview.equals("null")) overview = "";
        Overview.setText(overview);
        if (movie.getIsFavorite()) Favorite.setImageResource(R.drawable.ic_favorite);
        else Favorite.setImageResource(R.drawable.ic_favorite_border);
        videoAdapter.setMovie(movie);
        reviewAdapter.setMovie(movie);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Movie> loader) {}

    @Override
    public void startVideoActivity(int index) {
        Uri uri = Uri.parse(BASE_VIDEO_URL + movie.getVideo(index).getKey());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private static class MovieLoader extends AsyncTaskLoader<Movie> {
        private final Movie movie;

        MovieLoader(Context context, Movie m) {
            super(context);
            movie = m;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        @Nullable
        @Override
        public Movie loadInBackground() {
            ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info != null && info.isConnectedOrConnecting()) {
                URL url = null;
                try {
                    url = new URL(BASE_MOVIE_URL + movie.getId() + URL_API_KEY + movieDbApiKey);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                try {
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    try {
                        Scanner scanner = new Scanner(connection.getInputStream());
                        scanner.useDelimiter("\\A");
                        if (scanner.hasNext()) try {
                            Bitmap poster;
                            JSONObject json = new JSONObject(scanner.next());
                            movie.setTitle(json.getString("title"));
                            movie.setReleaseDate(json.getString("release_date"));
                            String posterPath = json.getString("poster_path");
                            if (posterPath.equals("null")) poster = null;
                            else poster = Picasso.with(getContext()).load(BASE_POSTER_URL + posterPath).get();
                            movie.setPoster(poster);
                            movie.setVoteAverage(json.getString("vote_average"));
                            movie.setOverview(json.getString("overview"));
                            if (movie.getIsFavorite()) {
                                byte[] posterArray;
                                ContentValues values = new ContentValues();
                                values.put(MovieContract.MovieEntry.COLUMN_TITLE, movie.getTitle());
                                values.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
                                if (poster == null) posterArray = new byte[0];
                                else {
                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                    poster.compress(Bitmap.CompressFormat.PNG, 0, stream);
                                    posterArray = stream.toByteArray();
                                }
                                values.put(MovieContract.MovieEntry.COLUMN_POSTER, posterArray);
                                values.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, movie.getVoteAverage());
                                values.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, movie.getOverview());
                                String selection = MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=?";
                                String[] selectionArgs = new String[] {String.valueOf(movie.getId())};
                                getContext().getContentResolver().update(
                                        MovieContract.MovieEntry.CONTENT_URI,
                                        values,
                                        selection,
                                        selectionArgs);
                            }
                        } catch (JSONException e) {
                            if (movie.getIsFavorite()) fetchFavorite();
                            else e.printStackTrace();
                        }
                    } catch (IOException e) {
                        if (movie.getIsFavorite()) fetchFavorite();
                        else e.printStackTrace();
                    }
                    connection.disconnect();
                } catch (IOException e) {
                    if (movie.getIsFavorite()) fetchFavorite();
                    else e.printStackTrace();
                }
                url = null;
                try {
                    url = new URL(BASE_MOVIE_URL + movie.getId() + URL_VIDEOS + URL_API_KEY + movieDbApiKey);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                try {
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    try {
                        Scanner scanner = new Scanner(connection.getInputStream());
                        scanner.useDelimiter("\\A");
                        if (scanner.hasNext()) try {
                            JSONObject json = new JSONObject(scanner.next());
                            JSONArray results = json.getJSONArray("results");
                            movie.clearVideos();
                            for (int i = 0; i < results.length(); i++) {
                                JSONObject result = results.getJSONObject(i);
                                String key = result.getString("key");
                                String name = result.getString("name");
                                movie.addVideo(key, name);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            movie.clearVideos();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    connection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                url = null;
                try {
                    url = new URL(BASE_MOVIE_URL + movie.getId() + URL_REVIEWS + URL_API_KEY + movieDbApiKey);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                try {
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    try {
                        Scanner scanner = new Scanner(connection.getInputStream());
                        scanner.useDelimiter("\\A");
                        if (scanner.hasNext()) try {
                            JSONObject json = new JSONObject(scanner.next());
                            JSONArray results = json.getJSONArray("results");
                            movie.clearReviews();
                            for (int i = 0; i < results.length(); i++) {
                                JSONObject result = results.getJSONObject(i);
                                String author = result.getString("author");
                                String content = result.getString("content");
                                movie.addReview(author, content);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            movie.clearVideos();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    connection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                fetchFavorite();
            }
            return movie;
        }

        void fetchFavorite() {
            String[] projection = new String[] {
                    MovieContract.MovieEntry.COLUMN_TITLE,
                    MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
                    MovieContract.MovieEntry.COLUMN_POSTER,
                    MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
                    MovieContract.MovieEntry.COLUMN_OVERVIEW};
            String selection = MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=?";
            String[] selectionArgs = new String[] {String.valueOf(movie.getId())};
            Cursor cursor = getContext().getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null);
            if (cursor != null && cursor.getCount() == 1) {
                cursor.moveToFirst();
                movie.setTitle(cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE)));
                movie.setReleaseDate(cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_DATE)));
                byte[] posterArray = cursor.getBlob(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER));
                movie.setPoster(BitmapFactory.decodeByteArray(posterArray, 0, posterArray.length));
                movie.setVoteAverage(cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE)));
                movie.setOverview(cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_OVERVIEW)));
                cursor.close();
            }
        }
    }
}
