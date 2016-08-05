package com.example.aliaksandrmirashnichenka.mediaplayer.managers;

import android.content.Context;
import android.support.annotation.NonNull;
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
public class VideoLoaderManager {

    private static final String TAG = VideoLoaderManager.class.getSimpleName();

    private final Context context;

    public VideoLoaderManager(@NonNull Context context) {
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
