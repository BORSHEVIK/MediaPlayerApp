package com.example.aliaksandrmirashnichenka.mediaplayer.fragments;

import android.app.Activity;
import android.graphics.Point;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.aliaksandrmirashnichenka.mediaplayer.R;
import com.example.aliaksandrmirashnichenka.mediaplayer.models.Movie;
import com.example.aliaksandrmirashnichenka.mediaplayer.util.ImageHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;

/**
 * This class is fragment which show splash screen
 *
 * Created by aliaksandrmirashnichenka on 01.08.16.
 */
@EFragment(R.layout.fragment_splash)
public class SplashFragment extends Fragment {

    private static final int MIN_SPLASH_TIME = 1000;  // milliseconds

    private static final int MAX_SPLASH_TIME = 10000; // milliseconds

    @ViewById(R.id.title)
    protected TextView mTitle;

    @ViewById(R.id.year)
    protected TextView mYear;

    @ViewById(R.id.description)
    protected TextView mDescription;

    @ViewById(R.id.image)
    protected ImageView mImage;

    @InstanceState
    protected static Movie mMovie;

    public void initInstance(@NonNull Movie movie) {
        mMovie = movie;
    }

    @AfterViews
    protected void initView() {
        initMovie(mMovie);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Use delay to closing splash screen (min = 1 sec)
        final Handler handler = new Handler();
        handler.postDelayed(() -> closeSplash(false), MIN_SPLASH_TIME);

        // Use delay to closing splash screen (max = 10 sec)
        handler.postDelayed(() -> closeSplash(true), MAX_SPLASH_TIME);
    }

    private void closeSplash(@NonNull boolean forceClose) {
        Activity activity = getActivity();
        if (activity instanceof PlayerFragmentListener) {
            PlayerFragmentListener listener = (PlayerFragmentListener) activity;
            listener.splashCanClose(forceClose);
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private void initMovie(@NonNull Movie movie) {
        mTitle.setText(movie.getTitle());
        mYear.setText(String.valueOf(movie.getMeta().getReleaseYear()));
        mDescription.setText(movie.getDescription());

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        mImage.setImageBitmap(ImageHelper.decodeSampledBitmapFromResource(getResources(), "images/" + movie.getImages().getPlaceholder(), width, width));
    }

    @Override
    public void onStop() {
        super.onStop();

        mTitle = null;
        mYear = null;
        mDescription = null;
        mImage = null;
    }
}
