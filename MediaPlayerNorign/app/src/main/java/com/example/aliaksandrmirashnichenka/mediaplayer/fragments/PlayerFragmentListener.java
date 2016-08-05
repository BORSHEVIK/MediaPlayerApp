package com.example.aliaksandrmirashnichenka.mediaplayer.fragments;

/**
 * This interface calling methods when video actions updated
 *
 * Created by aliaksandrmirashnichenka on 01.08.16.
 */
public interface PlayerFragmentListener {

    void movieLoaded();

    void splashCanClose(boolean forceClose);

    void videoChanged(long videoID);
}
