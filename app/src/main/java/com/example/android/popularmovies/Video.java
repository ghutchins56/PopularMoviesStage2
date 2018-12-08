package com.example.android.popularmovies;

class Video {
    private final String Key;
    private final String Name;

    Video(String key, String name) {
        Key = key;
        Name = name;
    }

    String getKey() {
        return Key;
    }

    String getName() {
        return Name;
    }
}
