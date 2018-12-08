package com.example.android.popularmovies;

class Review {
    private final String Author;
    private final String Content;

    Review(String author, String content) {
        Author = author;
        Content = content;
    }

    String getAuthor() {
        return Author;
    }

    String getContent() {
        return Content;
    }
}
