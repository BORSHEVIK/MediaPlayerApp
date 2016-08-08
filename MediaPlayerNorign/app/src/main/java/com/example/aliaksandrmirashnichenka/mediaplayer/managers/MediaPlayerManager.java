package com.example.aliaksandrmirashnichenka.mediaplayer.managers;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.aliaksandrmirashnichenka.mediaplayer.fragments.PlayerFragmentViewsListener;
import com.example.aliaksandrmirashnichenka.mediaplayer.R;
import com.example.aliaksandrmirashnichenka.mediaplayer.exoplayer.DashRendererBuilder;
import com.example.aliaksandrmirashnichenka.mediaplayer.exoplayer.DemoPlayer;
import com.example.aliaksandrmirashnichenka.mediaplayer.exoplayer.EventLogger;
import com.example.aliaksandrmirashnichenka.mediaplayer.exoplayer.ExtractorRendererBuilder;
import com.example.aliaksandrmirashnichenka.mediaplayer.exoplayer.HlsRendererBuilder;
import com.example.aliaksandrmirashnichenka.mediaplayer.exoplayer.SmoothStreamingRendererBuilder;
import com.example.aliaksandrmirashnichenka.mediaplayer.exoplayer.SmoothStreamingTestMediaDrmCallback;
import com.example.aliaksandrmirashnichenka.mediaplayer.exoplayer.WidevineTestMediaDrmCallback;
import com.example.aliaksandrmirashnichenka.mediaplayer.util.ErrorHelper;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.MediaCodecUtil;
import com.google.android.exoplayer.drm.UnsupportedDrmException;
import com.google.android.exoplayer.metadata.id3.GeobFrame;
import com.google.android.exoplayer.metadata.id3.Id3Frame;
import com.google.android.exoplayer.metadata.id3.PrivFrame;
import com.google.android.exoplayer.metadata.id3.TxxxFrame;
import com.google.android.exoplayer.util.Util;

import org.androidannotations.annotations.EBean;
import org.xml.sax.ErrorHandler;

import java.util.List;

/**
 * This class managing MediaPlayer
 *
 * Created by aliaksandrmirashnichenka on 05.08.16.
 */
@EBean
public class MediaPlayerManager implements DemoPlayer.Listener, DemoPlayer.Id3MetadataListener {

    private static final String TAG = MediaPlayerManager.class.getSimpleName();

    public static final boolean PLAYER_WHEN_READ = true;

    public static final boolean NOT_PLAYER_WHEN_READY = false;

    private EventLogger mEventLogger;

    private DemoPlayer mPlayer;

    private boolean mPlayerNeedsPrepare;

    private long mPlayerPosition;

    private boolean mPlayReady;

    private Uri mContentUri;

    private PlayerFragmentViewsListener mPlayerFragmentViewsListener;

    private MediaPlayerManagerListener mMediaPlayerManagerListener;

    /**
     * @param playerFragmentViewsListener - Views receiver of root View
     * @param mediaPlayerManagerListener - Transmitter of actions
     */
    public void initMediaPlayerManager(
                                       @NonNull PlayerFragmentViewsListener playerFragmentViewsListener,
                                       @NonNull MediaPlayerManagerListener mediaPlayerManagerListener) {
        this.mPlayerFragmentViewsListener = playerFragmentViewsListener;
        this.mMediaPlayerManagerListener = mediaPlayerManagerListener;
        this.mPlayReady = NOT_PLAYER_WHEN_READY;
    }

    /**
     * @param contentType -  type of content
     */
    public void preparePlayer(@NonNull int contentType) {

        if (mPlayer == null) {
            mPlayer = new DemoPlayer(getRendererBuilder(contentType));
            mPlayer.addListener(this);
            mPlayer.setMetadataListener(this);
            mPlayer.seekTo(mPlayerPosition);
            mPlayerNeedsPrepare = true;
            mPlayerFragmentViewsListener.getMediaController().setMediaPlayer(mPlayer.getPlayerControl());
            mPlayerFragmentViewsListener.getMediaController().setEnabled(true);
            mEventLogger = new EventLogger();
            mEventLogger.startSession();
            mPlayer.addListener(mEventLogger);
            mPlayer.setInfoListener(mEventLogger);
            mPlayer.setInternalErrorListener(mEventLogger);
        }
        if (mPlayerNeedsPrepare) {
            mPlayer.prepare();
            mPlayerNeedsPrepare = false;
        }
        mPlayer.setSurface(mPlayerFragmentViewsListener.getSufraceView().getHolder().getSurface());
        mPlayer.setPlayWhenReady(mPlayReady);
    }

    public void releasePlayer() {
        if (mPlayer != null) {
            mPlayerPosition = mPlayer.getCurrentPosition();
            mPlayer.removeListener(this);
            mPlayer.removeListener(mEventLogger);
            mPlayer.setMetadataListener(null);
            mPlayer.setInfoListener(null);
            mPlayer.setInternalErrorListener(null);
            mPlayer.release();
            mPlayer = null;
            mEventLogger.endSession();
            mEventLogger = null;
        }
    }

    /**
     * Returns DemoPlayer.RendererBuilder for a given contentType
     *
     * @return DemoPlayer.RendererBuilder instance
     */
    private DemoPlayer.RendererBuilder getRendererBuilder(@NonNull int contentType) {
        String userAgent = Util.getUserAgent(mPlayerFragmentViewsListener.getRootActivity(), "ExoPlayerDemo");
        switch (contentType) {
            case Util.TYPE_SS:
                return new SmoothStreamingRendererBuilder(mPlayerFragmentViewsListener.getRootContenxt(), userAgent, mContentUri.toString(),
                        new SmoothStreamingTestMediaDrmCallback());
            case Util.TYPE_DASH:
                return new DashRendererBuilder(mPlayerFragmentViewsListener.getRootActivity(), userAgent, mContentUri.toString(),
                        new WidevineTestMediaDrmCallback("", ""));
            case Util.TYPE_HLS:
                return new HlsRendererBuilder(mPlayerFragmentViewsListener.getRootContenxt(), userAgent, mContentUri.toString());
            case Util.TYPE_OTHER:
                return new ExtractorRendererBuilder(mPlayerFragmentViewsListener.getRootContenxt(), userAgent, mContentUri);
            default:
                throw new IllegalStateException("Unsupported type: " + contentType);
        }
    }


    /**
     * @return - actual state of media mPlayer
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean getPlayerReadyState() {
        return this.mPlayReady;
    }

    /**
     * @param readyState - state of media mPlayer
     */
    public void setPlayerReadyState(@NonNull boolean readyState) {
        this.mPlayReady = readyState;
    }

    /**
     * @param contentUri - Uri for movie content
     */
    public void setContentUri(@NonNull Uri contentUri) {
        this.mContentUri = contentUri;
    }

    /**
     * @return - actual Uri for movie content
     */
    @SuppressWarnings("unused")
    public Uri getContentUri() {
        return this.mContentUri;
    }

    /**
     * @return - instance of mPlayer
     */
    public DemoPlayer getPlayer() {
        return mPlayer;
    }

    @Override
    public void onId3Metadata(@NonNull List<Id3Frame> id3Frames) {
        for (Id3Frame id3Frame : id3Frames) {
            if (id3Frame instanceof TxxxFrame) {
                TxxxFrame txxxFrame = (TxxxFrame) id3Frame;
                Log.i(TAG, String.format("ID3 TimedMetadata %s: description=%s, value=%s", txxxFrame.id, txxxFrame.description, txxxFrame.value));
            } else if (id3Frame instanceof PrivFrame) {
                PrivFrame privFrame = (PrivFrame) id3Frame;
                Log.i(TAG, String.format("ID3 TimedMetadata %s: owner=%s", privFrame.id, privFrame.owner));
            } else if (id3Frame instanceof GeobFrame) {
                GeobFrame geobFrame = (GeobFrame) id3Frame;
                Log.i(TAG, String.format("ID3 TimedMetadata %s: mimeType=%s, filename=%s, description=%s",
                        geobFrame.id, geobFrame.mimeType, geobFrame.filename, geobFrame.description));
            } else {
                Log.i(TAG, String.format("ID3 TimedMetadata %s", id3Frame.id));
            }
        }
    }

    @Override
    public void onStateChanged(@NonNull boolean playWhenReady,
                               @NonNull int playbackState) {
        if (playbackState == ExoPlayer.STATE_ENDED) {
            mMediaPlayerManagerListener.needShowControls();
        }
        Log.d(TAG, "onStateChanged() called with: " + "playWhenReady = [" + playWhenReady + "], playbackState = [" + playbackState + "]");
        boolean showProgress = true;
        String text = "state: ";
        switch (playbackState) {
            case ExoPlayer.STATE_BUFFERING:
                text += "buffering";
                break;
            case ExoPlayer.STATE_ENDED:
                text += "ended";
                showProgress = false;
                break;
            case ExoPlayer.STATE_IDLE:
                text += "idle";
                showProgress = false;
                break;
            case ExoPlayer.STATE_PREPARING:
                text += "preparing";
                break;
            case ExoPlayer.STATE_READY:
                showProgress = false;
                text += "ready";
                mMediaPlayerManagerListener.needUpdateActivity();
                break;
            default:
                text += "unknown";
                break;
        }
        mPlayerFragmentViewsListener.getProgressBar().setVisibility(showProgress ? View.VISIBLE : View.GONE);
        Log.d(TAG, text);
    }

    @Override
    public void onError(@NonNull Exception e) {
        String errorString = null;

        errorString = ErrorHelper.getBuildMessageFromError(e, mPlayerFragmentViewsListener.getRootContenxt());

        if (errorString == null) {
            errorString = e.getMessage();
        }
        if (errorString != null) {
            Toast.makeText(mPlayerFragmentViewsListener.getRootContenxt(), errorString, Toast.LENGTH_LONG).show();
        }
        mPlayerNeedsPrepare = true;
        mMediaPlayerManagerListener.needShowControls();
        mMediaPlayerManagerListener.needUpdateActivity();
    }

    @Override
    public void onVideoSizeChanged(@NonNull int width,
                                   @NonNull int height,
                                   @NonNull int unappliedRotationDegrees,
                                   @NonNull float pixelWidthHeightRatio) {
        mPlayerFragmentViewsListener.getFrameLayout().setAspectRatio(height == 0 ? 1 : (width * pixelWidthHeightRatio) / height);
    }
}
