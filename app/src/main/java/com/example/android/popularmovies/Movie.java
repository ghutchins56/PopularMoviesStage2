package com.example.android.popularmovies;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class Movie implements Parcelable {
    private final long Id;
    private String Title;
    private String ReleaseDate;
    private Bitmap Poster;
    private String VoteAverage;
    private String Overview;
    private boolean IsFavorite;
    private final ArrayList<Video> Videos;
    private final ArrayList<Review> Reviews;

    Movie(long id, Bitmap poster, boolean isFavorite) {
        Id = id;
        Title = null;
        ReleaseDate = null;
        Poster = poster;
        VoteAverage = null;
        Overview = null;
        IsFavorite = isFavorite;
        Videos = null;
        Reviews = null;
    }

    private Movie(Parcel source) {
        Id = source.readLong();
        Title = "";
        ReleaseDate = "";
        byte[] posterArray = source.createByteArray();
        Poster = BitmapFactory.decodeByteArray(posterArray, 0, posterArray.length);
        VoteAverage = "";
        Overview = "";
        boolean[] isFavoriteArray = source.createBooleanArray();
        IsFavorite = isFavoriteArray[0];
        Videos = new ArrayList<>();
        Reviews = new ArrayList<>();
    }

    Movie() {
        Id = 0;
        Title = "";
        ReleaseDate = "";
        Poster = null;
        VoteAverage = "";
        Overview = "";
        IsFavorite = false;
        Videos = new ArrayList<>();
        Reviews = new ArrayList<>();
    }

    long getId() {
        return Id;
    }

    String getTitle() {
        return Title;
    }

    void setTitle(String title) {
        Title = title;
    }

    String getReleaseDate() {
        return ReleaseDate;
    }

    void setReleaseDate(String releaseDate) {
        ReleaseDate = releaseDate;
    }

    Bitmap getPoster() {
        return Poster;
    }

    void setPoster (Bitmap poster) {
        Poster = poster;
    }

    String getVoteAverage() {
        return VoteAverage;
    }

    void setVoteAverage(String voteAverage) {
        VoteAverage = voteAverage;
    }

    String getOverview() {
        return Overview;
    }

    void setOverview(String overview) {
        Overview = overview;
    }

    boolean getIsFavorite() {
        return IsFavorite;
    }

    void setIsFavorite(boolean isFavorite) {
        IsFavorite = isFavorite;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(Id);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Poster.compress(Bitmap.CompressFormat.PNG, 0, stream);
        byte[] posterArray = stream.toByteArray();
        dest.writeByteArray(posterArray);
        dest.writeBooleanArray(new boolean[] {IsFavorite});
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {

        @Override
        public Movie createFromParcel(Parcel source) {
            return new Movie(source);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    void addVideo(String key, String name) {
        Videos.add(new Video(key, name));
    }

    int getVideosSize() {
        return Videos.size();
    }

    void clearVideos() {
        Videos.clear();
    }

    Video getVideo(int index) {
        return Videos.get(index);
    }

    void addReview(String author, String content) {
        Reviews.add(new Review(author, content));
    }

    int getReviewsSize() {
        return Reviews.size();
    }

    void clearReviews() {
        Reviews.clear();
    }

    Review getReview(int index) {
        return Reviews.get(index);
    }
}
