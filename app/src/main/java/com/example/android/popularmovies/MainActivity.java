package com.example.android.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity implements MainActivityInterface, LoaderCallbacks<ArrayList<Movie>> {
    private static final int SORT_ORDER_FAVORITES = 1;
    private static final int SORT_ORDER_POPULAR = 2;
    private static final int SORT_ORDER_TOP_RATED = 3;
    private MovieAdapter movieAdapter;
    private TextView errorMessage;
    private ProgressBar loadingIndicator;
    private RecyclerView movieGrid;
    private static final int LOADER_ID = 1;
    private static final String BASE_URL = "https://api.themoviedb.org/3/movie/";
    private static final String URL_POPULAR = "popular?api_key=";
    private static final String URL_TOP_RATED = "top_rated?api_key=";
    private String movieDbApiKey;
    private static final String KEY_MOVIE = "movie";
    private static final String KEY_MOVIES = "movies";
    private static final String KEY_SORT_ORDER = "sort_order";
    private static final String BASE_POSTER_URL = "https://image.tmdb.org/t/p/w185";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        movieGrid = findViewById(R.id.movies);
        errorMessage = findViewById(R.id.error_message);
        loadingIndicator = findViewById(R.id.loading_indicator);
        movieDbApiKey = BuildConfig.MOVIE_DB_API_KEY;
        int spanCount = getResources().getInteger(R.integer.span_count);
        movieGrid.setLayoutManager(new GridLayoutManager(this, spanCount));
        movieAdapter = new MovieAdapter(this);
        movieGrid.setAdapter(movieAdapter);
        if (savedInstanceState == null) {
            loadMovies(SORT_ORDER_POPULAR);
        } else {
            ArrayList<Movie> movies = savedInstanceState.getParcelableArrayList(KEY_MOVIES);
            movieAdapter.setMovies(movies);
            loadingIndicator.setVisibility(View.INVISIBLE);
            if (movies.isEmpty()) {
                errorMessage.setVisibility(View.VISIBLE);
                movieGrid.setVisibility(View.INVISIBLE);
            } else {
                errorMessage.setVisibility(View.INVISIBLE);
                movieGrid.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(KEY_MOVIES, movieAdapter.getMovies());
        super.onSaveInstanceState(outState);
    }

    public void startDetailActivity(Movie movie) {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if ((info != null && info.isConnectedOrConnecting()) || movie.getIsFavorite()) {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(KEY_MOVIE, movie);
            startActivity(intent);
        } else Toast.makeText(this, getString(R.string.error_message), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_most_popular) {
            loadMovies(SORT_ORDER_POPULAR);
            return true;
        }

        if (id == R.id.action_highest_rated) {
            loadMovies(SORT_ORDER_TOP_RATED);
            return true;
        }

        if (id == R.id.action_favorites) {
            loadMovies(SORT_ORDER_FAVORITES);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public Loader<ArrayList<Movie>> onCreateLoader(int id, @Nullable Bundle args) {
        switch (id) {
            case LOADER_ID:
                return new MovieLoader(this, args.getInt(KEY_SORT_ORDER), movieDbApiKey);
            default:
                throw new RuntimeException("Loader ID " + id + " not implemented");
        }
    }

    private static class MovieLoader extends AsyncTaskLoader<ArrayList<Movie>> {
        final int SortOrder;
        final String ApiKey;

        MovieLoader(Context context, int sortOrder, String apiKey) {
            super(context);
            SortOrder = sortOrder;
            ApiKey = apiKey;
        }

        @Nullable
        @Override
        public ArrayList<Movie> loadInBackground() {
            switch (SortOrder) {
                case SORT_ORDER_POPULAR:
                    return FetchMovies();
                case SORT_ORDER_TOP_RATED:
                    return FetchMovies();
                case SORT_ORDER_FAVORITES:
                    ArrayList<Movie> movies = new ArrayList<>();
                    String[] projection = new String[] {
                            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
                            MovieContract.MovieEntry.COLUMN_POSTER};
                    Cursor cursor = getContext().getContentResolver().query(
                            MovieContract.MovieEntry.CONTENT_URI,
                            projection,
                            null,
                            null,
                            null);
                    if (cursor != null && cursor.getCount() > 0) {
                        while (!cursor.isLast()) {
                            cursor.moveToNext();
                            long id = cursor.getLong(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID));
                            byte[] posterArray = cursor.getBlob(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER));
                            Bitmap poster = BitmapFactory.decodeByteArray(posterArray, 0, posterArray.length);
                            movies.add(new Movie(id, poster, true));
                        }
                        cursor.close();
                    }
                    return movies;
                default:
                    return null;
            }
        }

        ArrayList<Movie> FetchMovies() {
            ArrayList<Movie> movies = new ArrayList<>();
            ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info != null && info.isConnectedOrConnecting()) {
                URL url = null;
                try {
                    switch (SortOrder) {
                        case SORT_ORDER_POPULAR:
                            url = new URL(BASE_URL + URL_POPULAR + ApiKey);
                            break;
                        case SORT_ORDER_TOP_RATED:
                            url = new URL(BASE_URL + URL_TOP_RATED + ApiKey);
                    }
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
                            for (int i = 0; i < results.length(); i++) {
                                byte[] posterArray;
                                Bitmap poster;
                                JSONObject result = results.getJSONObject(i);
                                long id = result.getLong("id");
                                String posterPath = result.getString("poster_path");
                                if (posterPath.equals("null")) {
                                    posterArray = new byte[0];
                                    poster = null;
                                } else {
                                    poster = Picasso.with(getContext()).load(BASE_POSTER_URL + posterPath).get();
                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                    poster.compress(Bitmap.CompressFormat.PNG, 0, stream);
                                    posterArray = stream.toByteArray();
                                }
                                ContentValues values = new ContentValues();
                                values.put(MovieContract.MovieEntry.COLUMN_POSTER, posterArray);
                                String selection = MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=?";
                                String[] selectionArgs = new String[] {
                                        String.valueOf(id)};
                                int rowsUpdated = getContext().getContentResolver().update(
                                        MovieContract.MovieEntry.CONTENT_URI,
                                        values,
                                        selection,
                                        selectionArgs);
                                boolean isFavorite = false;
                                if (rowsUpdated > 0) {
                                    isFavorite = true;
                                }
                                movies.add(new Movie(id, poster, isFavorite));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            movies.clear();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    connection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return movies;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<ArrayList<Movie>> loader, ArrayList<Movie> data) {
        loadingIndicator.setVisibility(View.INVISIBLE);
        movieAdapter.setMovies(data);
        if (data.isEmpty()) {
            errorMessage.setVisibility(View.VISIBLE);
            movieGrid.setVisibility(View.INVISIBLE);
        } else {
            errorMessage.setVisibility(View.INVISIBLE);
            movieGrid.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<ArrayList<Movie>> loader) {}

    private void loadMovies(int sortOrder) {
        errorMessage.setVisibility(View.INVISIBLE);
        loadingIndicator.setVisibility(View.VISIBLE);
        movieGrid.setVisibility(View.INVISIBLE);
        Bundle args = new Bundle();
        args.putInt(KEY_SORT_ORDER, sortOrder);
        getSupportLoaderManager().restartLoader(LOADER_ID, args, this);
    }
}
