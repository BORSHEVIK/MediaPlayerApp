package com.example.aliaksandrmirashnichenka.mediaplayer.datastorage;

import android.content.Context;
import android.util.Log;

import com.bluelinelabs.logansquare.LoganSquare;
import com.example.aliaksandrmirashnichenka.mediaplayer.models.Movie;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for loading movies from storage
 *
 * Created by aliaksandrmirashnichenka on 01.08.16.
 */
public class MovieStorage {

    private static final String TAG = MovieStorage.class.getSimpleName();

    private Context context;

    public MovieStorage(Context context) {
        this.context = context;
    }

    /**
     * Parse movies.json
     *
     * @return array of loaded movies
     */
    public List<Movie> loadMovies() {
        try {
            return LoganSquare.parseList(context.getAssets().open("movies.json"), Movie.class);
        } catch (IOException e) {
            Log.e(TAG, "loadMovies", e);
        }
        return new ArrayList<>();
    }
}
