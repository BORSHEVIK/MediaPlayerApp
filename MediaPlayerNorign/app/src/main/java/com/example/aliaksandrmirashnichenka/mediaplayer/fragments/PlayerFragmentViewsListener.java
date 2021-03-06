package com.example.aliaksandrmirashnichenka.mediaplayer.fragments;

import android.app.Activity;
import android.content.Context;
import android.view.SurfaceView;
import android.widget.MediaController;
import android.widget.ProgressBar;

import com.google.android.exoplayer.AspectRatioFrameLayout;

/**
 * This interface provide View for manager
 *
 * Created by aliaksandrmirashnichenka on 05.08.16.
 */
public interface PlayerFragmentViewsListener {

    MediaController getMediaController();
    ProgressBar getProgressBar();
    AspectRatioFrameLayout getFrameLayout();
    SurfaceView getSufraceView();
    Activity getRootActivity();
    Context getRootContenxt();

}
