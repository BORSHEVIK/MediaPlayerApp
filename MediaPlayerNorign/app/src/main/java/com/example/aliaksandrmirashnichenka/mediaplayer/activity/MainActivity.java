package com.example.aliaksandrmirashnichenka.mediaplayer.activity;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

import com.example.aliaksandrmirashnichenka.mediaplayer.BuildConfig;
import com.example.aliaksandrmirashnichenka.mediaplayer.fragments.PlayerFragmentListener;
import com.example.aliaksandrmirashnichenka.mediaplayer.fragments.PlayerFragment;
import com.example.aliaksandrmirashnichenka.mediaplayer.fragments.PlayerFragment_;
import com.example.aliaksandrmirashnichenka.mediaplayer.fragments.SplashFragment;
import com.example.aliaksandrmirashnichenka.mediaplayer.fragments.SplashFragment_;
import com.example.aliaksandrmirashnichenka.mediaplayer.R;
import com.example.aliaksandrmirashnichenka.mediaplayer.managers.VideoLoaderManager;
import com.example.aliaksandrmirashnichenka.mediaplayer.models.Movie;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.UiThread;

import java.util.ArrayList;

@SuppressLint("Registered")
@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity implements PlayerFragmentListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private boolean mMovieLoaded;

    private boolean mCanCloseSplash;

    private ArrayList<Movie> mMovieList;

    @SuppressWarnings("WeakerAccess")
    @InstanceState
    protected boolean mReplaceVideo;

    @SuppressWarnings("WeakerAccess")
    @InstanceState
    protected String mMovieId;

    private boolean mInSavedState;

    @SuppressWarnings("unused")
    @AfterViews
    protected void initActivity() {
        if (!mReplaceVideo) {
            mReplaceVideo = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mInSavedState = false;

        updateScreenOrientation();

        if (mMovieId == null ) {
            mMovieId = BuildConfig.MOVIE_ID;
        }
        loadData();
    }

    /**
     * Invoke loading data from json file if movieList is null (not loaded yet)
     *
     */
    @SuppressWarnings("WeakerAccess")
    @Background
    protected void loadData() {
        try {
            ArrayList<Movie> list;
            if (mMovieList != null) {
                list = mMovieList;
            } else {
                VideoLoaderManager movieStorage = new VideoLoaderManager(getApplicationContext());
                list = new ArrayList<>(movieStorage.loadMovies());
            }
            loadDataComplete(list);
        } catch(Throwable throwable) {
            Log.e(TAG, "startLoadData", throwable);
        }
    }

    @SuppressWarnings("WeakerAccess")
    @UiThread
    protected void loadDataComplete(ArrayList<Movie> list) {
        mMovieList = list;
        showFragments(mMovieId);
    }

    /**
     * Attach video fragment and splash fragment to the activity
     *
     * @param id movie id
     */
    private void showFragments(@NonNull String id) {
        if (!mReplaceVideo) {
            return;
        }

        mMovieLoaded = false;
        mCanCloseSplash = false;

        Movie movie = null;
        for (Movie localMovie : mMovieList) {
            if (String.valueOf(localMovie.getID()).equals(id)) {
                movie = localMovie;
                break;
            }
        }

        FragmentManager fragmentManager = getSupportFragmentManager();

        PlayerFragment playerFragment = PlayerFragment_.builder().build();
        playerFragment.initInstance(movie, mMovieList);
        fragmentManager.beginTransaction().replace(R.id.fragment_container_video, playerFragment).commit();

        SplashFragment splashFragment = SplashFragment_.builder().build();
        splashFragment.initInstance(movie);
        fragmentManager.beginTransaction().add(R.id.fragment_container_splash, splashFragment).commit();

        mReplaceVideo = false;
    }

    @Override
    protected void onStop() {
        super.onStop();

        mInSavedState = true;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        updateScreenOrientation();
    }

    /**
     * Set fullscreen mode for landscape orientation
     *
     * @param visible status bar visibility flag
     */
    private void setFullscreen(boolean visible) {
        if (visible) {
            // Hide status bar
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            // Show status bar
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    /**
     * Close splash screen if movie is ready to play and splash was showing at least 1 second
     */
    private void closeSplash() {
        if (!mCanCloseSplash || !mMovieLoaded || mInSavedState) {
            return;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();

        Fragment splashFragment = fragmentManager.findFragmentById(R.id.fragment_container_splash);

        if (splashFragment != null) {
            fragmentManager.beginTransaction().remove(splashFragment).commit();
        }

        Fragment playerFragment = fragmentManager.findFragmentById(R.id.fragment_container_video);

        if (playerFragment instanceof PlayerFragment) {
            // Invoke playing the video after splash is closed
            ((PlayerFragment) playerFragment).playWhenReady();
        }
    }

    /**
     * Update UI depends on orientation
     */
    private void updateScreenOrientation() {
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            setFullscreen(false);
        } else {
            setFullscreen(true);
        }
    }

    //PlayerFragmentListener implementation

    @Override
    public void movieLoaded() {
        mMovieLoaded = true;
        closeSplash();
    }

    @Override
    public void splashCanClose(boolean forceClose) {
        mCanCloseSplash = true;
        if (forceClose) {
            mMovieLoaded = true;
        }
        closeSplash();
    }

    @Override
    public void videoChanged(long videoID) {
        mReplaceVideo = true;
        mMovieId = String.valueOf(videoID);
        loadData();
    }
}
