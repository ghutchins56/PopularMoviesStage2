package com.example.android.popularmovies;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoAdapterViewHolder> {
    private Movie M = new Movie();
    private final DetailActivityInterface ActivityInterface;

    public VideoAdapter(DetailActivityInterface activityInterface) {
        super();
        ActivityInterface = activityInterface;
    }

    public class VideoAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView name;

        VideoAdapterViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.name);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            ActivityInterface.startVideoActivity(getAdapterPosition());
        }
    }

    @NonNull
    @Override
    public VideoAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.video_item, parent, false);
        return new VideoAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoAdapterViewHolder holder, int position) {
        holder.name.setText(M.getVideo(position).getName());
    }

    @Override
    public int getItemCount() {
        return M.getVideosSize();
    }

    public void setMovie(Movie m) {
        M = m;
        notifyDataSetChanged();
    }
}
