package com.example.android.popularmovies;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewAdapterViewHolder> {
    private Movie M = new Movie();

    public class ReviewAdapterViewHolder extends RecyclerView.ViewHolder {
        final TextView content;
        final TextView author;

        ReviewAdapterViewHolder(View v) {
            super(v);
            content = v.findViewById(R.id.content);
            author = v.findViewById(R.id.author);
        }
    }

    @NonNull
    @Override
    public ReviewAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.review_item, parent, false);
        return new ReviewAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewAdapterViewHolder holder, int position) {
        holder.content.setText(M.getReview(position).getContent());
        holder.author.setText(M.getReview(position).getAuthor());
    }

    @Override
    public int getItemCount() {
        return M.getReviewsSize();
    }

    public void setMovie(Movie m) {
        M = m;
        notifyDataSetChanged();
    }
}
