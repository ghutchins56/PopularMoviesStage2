package com.example.android.popularmovies;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {
    private ArrayList<Movie> Movies = new ArrayList<>();
    private final MainActivityInterface ActivityInterface;

    MovieAdapter(MainActivityInterface activityInterface) {
        super();
        ActivityInterface = activityInterface;
    }

    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView posterView;

        MovieAdapterViewHolder(View v) {
            super(v);
            posterView = v.findViewById(R.id.poster_view);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            ActivityInterface.startDetailActivity(Movies.get(getAdapterPosition()));
        }
    }

    @NonNull
    @Override
    public MovieAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.movie_item, parent, false);
        return new MovieAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieAdapterViewHolder holder, int position) {
        Bitmap poster = Movies.get(position).getPoster();
        if (poster == null) {
            holder.posterView.setImageResource(R.drawable.popcorn_rendered);
        } else {
            holder.posterView.setImageBitmap(poster);
        }
    }

    @Override
    public int getItemCount() {
        return Movies.size();
    }

    public void setMovies(ArrayList<Movie> movies) {
        Movies = movies;
        notifyDataSetChanged();
    }

    public ArrayList<Movie> getMovies() {
        return Movies;
    }
}
