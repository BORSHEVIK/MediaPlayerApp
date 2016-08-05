package com.example.aliaksandrmirashnichenka.mediaplayer.fragments;

/**
 * Created by aliaksandrmirashnichenka on 01.08.16.
 */
@SuppressWarnings("ALL")
public interface PlayerFragmentListener {

    void movieLoaded();

    void splashCanClose(boolean forceClose);

    void videoChanged(long videoID);
}
