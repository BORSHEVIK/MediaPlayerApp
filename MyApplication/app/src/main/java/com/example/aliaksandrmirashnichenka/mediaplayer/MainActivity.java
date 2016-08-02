package com.example.aliaksandrmirashnichenka.mediaplayer;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

import com.example.aliaksandrmirashnichenka.mediaplayer.Fragments.FragmentListener;
import com.example.aliaksandrmirashnichenka.mediaplayer.Fragments.PlayerFragment;
import com.example.aliaksandrmirashnichenka.mediaplayer.Fragments.PlayerFragment_;
import com.example.aliaksandrmirashnichenka.mediaplayer.Fragments.SplashFragment;
import com.example.aliaksandrmirashnichenka.mediaplayer.Fragments.SplashFragment_;
import com.example.aliaksandrmirashnichenka.mediaplayer.datastorage.MovieStorage;
import com.example.aliaksandrmirashnichenka.mediaplayer.models.Movie;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.UiThread;

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity implements FragmentListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private boolean mMovieLoaded;

    private boolean mCanCloseSplash;

    private ArrayList<Movie> mMovieList;

    @InstanceState
    protected boolean mReplaceVideo;

    @InstanceState
    protected String mMovieId;

    private boolean mInSavedState;

    @AfterViews
    private void initActivity() {
        if (!mReplaceVideo) {
            mReplaceVideo = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mInSavedState = false;

        updateScreenOrientation();

        if (mMovieId == null) {
            mMovieId = BuildConfig.MOVIE_ID;
        }
        loadData(mMovieId);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        mReplaceVideo = true;
    }

    /**
     * Invoke loading data from json file if movieList is null (not loaded yet)
     *
     * @param id movie id
     */
    @Background
    private void loadData(String id) {
        try {
            ArrayList<Movie> list;
            if (mMovieList != null) {
                list = mMovieList;
            } else {
                MovieStorage movieStorage = new MovieStorage(getApplicationContext());
                list = new ArrayList<>(movieStorage.loadMovies());
            }
            loadDataComplete(mMovieList);
        } catch(Throwable throwable) {
            Log.e(TAG, "startLoadData", throwable);
        }
    }

    @UiThread
    private void loadDataComplete(ArrayList<Movie> list) {
        mMovieList = list;
        showFragments(mMovieId);
    }

    /**
     * Attach video fragment and splash fragment to the activity
     *
     * @param id movie id
     */
    private void showFragments(String id) {
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

        //PlayerFragment videoFragment = PlayerFragment_.builder().build();
        //videoFragment.
        //PlayerFragment videoFragment = PlayerFragment.getInstance(movie, movieList);
        //fragmentManager.beginTransaction().replace(R.id.fragment_container_video, videoFragment).commit();

        SplashFragment splashFragment = SplashFragment_.builder().build();
        splashFragment.initMovie(movie);
        fragmentManager.beginTransaction().add(R.id.fragment_container_splash, splashFragment).commit();

        mReplaceVideo = false;
    }

    @Override
    protected void onStop() {
        super.onStop();

        mInSavedState = true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
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
            //((PlayerFragment) videoFragment).playWhenReady();
        }
    }

    /**
     * Update UI depends on orientation
     */
    public void updateScreenOrientation() {
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            setFullscreen(false);
        } else {
            setFullscreen(true);
        }
    }

    @Override
    public void movieLoaded() {

    }

    @Override
    public void splashCanClose(boolean forceClose) {

    }
}
