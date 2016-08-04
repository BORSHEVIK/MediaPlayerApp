package com.example.aliaksandrmirashnichenka.mediaplayer.Fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.aliaksandrmirashnichenka.mediaplayer.MainActivity;
import com.example.aliaksandrmirashnichenka.mediaplayer.MainActivity_;
import com.example.aliaksandrmirashnichenka.mediaplayer.R;
import com.example.aliaksandrmirashnichenka.mediaplayer.exoplayer.DashRendererBuilder;
import com.example.aliaksandrmirashnichenka.mediaplayer.exoplayer.DemoPlayer;
import com.example.aliaksandrmirashnichenka.mediaplayer.exoplayer.EventLogger;
import com.example.aliaksandrmirashnichenka.mediaplayer.exoplayer.ExtractorRendererBuilder;
import com.example.aliaksandrmirashnichenka.mediaplayer.exoplayer.HlsRendererBuilder;
import com.example.aliaksandrmirashnichenka.mediaplayer.exoplayer.SmoothStreamingRendererBuilder;
import com.example.aliaksandrmirashnichenka.mediaplayer.exoplayer.SmoothStreamingTestMediaDrmCallback;
import com.example.aliaksandrmirashnichenka.mediaplayer.exoplayer.WidevineTestMediaDrmCallback;
import com.example.aliaksandrmirashnichenka.mediaplayer.models.Movie;
import com.example.aliaksandrmirashnichenka.mediaplayer.util.ImageHelper;
import com.google.android.exoplayer.AspectRatioFrameLayout;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.MediaCodecUtil;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
import com.google.android.exoplayer.drm.UnsupportedDrmException;
import com.google.android.exoplayer.metadata.id3.GeobFrame;
import com.google.android.exoplayer.metadata.id3.Id3Frame;
import com.google.android.exoplayer.metadata.id3.PrivFrame;
import com.google.android.exoplayer.metadata.id3.TxxxFrame;
import com.google.android.exoplayer.util.Util;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aliaksandrmirashnichenka on 01.08.16.
 */
@EFragment(R.layout.fragment_video)
public class PlayerFragment extends Fragment  implements SurfaceHolder.Callback,
        DemoPlayer.Listener, DemoPlayer.Id3MetadataListener,
        AudioCapabilitiesReceiver.Listener  {

    private static final String TAG = PlayerFragment.class.getSimpleName();

    private static final CookieManager defaultCookieManager;

    static {
        defaultCookieManager = new CookieManager();
        defaultCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private EventLogger eventLogger;

    private DemoPlayer player;

    private boolean playerNeedsPrepare;

    private long playerPosition;

    private Uri contentUri;

    private int contentType;

    private AudioCapabilitiesReceiver audioCapabilitiesReceiver;

    private boolean mPlayReady;

    //
    @ViewById(R.id.video_frame)
    protected AspectRatioFrameLayout videoFrame;

    @ViewById(R.id.surface_view)
    protected SurfaceView surfaceView;

    private MediaController mediaController;

    @ViewById(R.id.navigation_panel)
    protected LinearLayout navigationLayout;

    @ViewById(R.id.progress)
    protected ProgressBar progressBar;

    @ViewById(R.id.root)
    protected View root;
    //

    @InstanceState
    protected ArrayList<Movie> mMovies;

    @InstanceState
    protected Movie mCurrentMovie;

    public void initInstance(Movie movie, ArrayList<Movie> movies) {
        mMovies = movies;
        mCurrentMovie = movie;
    }

    @AfterViews
    protected void initView() {

        if (!mPlayReady) {
            mPlayReady = false;
        }

        root.setOnTouchListener((view1, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                toggleControlsVisibility();
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                view1.performClick();
            }
            return true;
        });
        root.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE
                        || keyCode == KeyEvent.KEYCODE_MENU) {
                    return false;
                }
                return mediaController.dispatchKeyEvent(event);
            }
        });

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        videoFrame.setAspectRatio(1f * size.x / size.y);

        surfaceView.getHolder().addCallback(PlayerFragment.this);

        mediaController = new KeyCompatibleMediaController(getContext());
        mediaController.setAnchorView(root);

        CookieHandler currentHandler = CookieHandler.getDefault();
        if (currentHandler != defaultCookieManager) {
            CookieHandler.setDefault(defaultCookieManager);
        }

        audioCapabilitiesReceiver = new AudioCapabilitiesReceiver(getContext(), PlayerFragment.this);
        audioCapabilitiesReceiver.register();

        List<Movie> movieList = mMovies;

        for (int i = 0; i < movieList.size(); i++) {
            final Movie movie = movieList.get(i);
            final ImageView imageView = (ImageView) navigationLayout.getChildAt(i);

            ViewTreeObserver viewTreeObserver = imageView.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        imageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        int width = imageView.getWidth();
                        int height = imageView.getHeight();
                        imageView.setImageBitmap(ImageHelper.decodeSampledBitmapFromResource(getResources(),
                                "images/" + movie.getImages().getCover(), width, height));
                    }
                });
            }

            imageView.setOnClickListener(v -> {
                PlayerFragmentListener listener = (PlayerFragmentListener) PlayerFragment.this.getActivity();
                if (listener != null) {
                    listener.videoChanged(movie.getID());
                }
                /*
                Intent startIntent = new Intent(getContext(), MainActivity_.class);
                startIntent.putExtra(MainActivity.MOVIE_ID, movie.getID());
                startActivity(startIntent);
                */
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (Util.SDK_INT > 23) {
            onShown();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Util.SDK_INT <= 23 || player == null) {
            onShown();
        }
    }

    /**
     * Set flag to player to play the video when it will be ready
     */
    public void playWhenReady() {
        preparePlayer(true);
    }

    /**
     * Get movie instance from arguments and initialize the player
     */
    private void onShown() {
        Movie movie = mCurrentMovie;
        contentUri = Uri.parse(movie.getStreams().getUrl());
        // Use only this type
        contentType = Util.TYPE_OTHER;

        if (player == null) {
            if (!maybeRequestPermission()) {
                preparePlayer(mPlayReady);
            }
        } else {
            player.setBackgrounded(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (Util.SDK_INT <= 23) {
            onHidden();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (Util.SDK_INT > 23) {
            onHidden();
        }
    }


    /**
     * Release the player when fragment goes to pause
     */
    private void onHidden() {
        releasePlayer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        audioCapabilitiesReceiver.unregister();
        releasePlayer();
    }

    // AudioCapabilitiesReceiver.Listener methods

    @Override
    public void onAudioCapabilitiesChanged(AudioCapabilities audioCapabilities) {
        if (player == null) {
            return;
        }
        boolean backgrounded = player.getBackgrounded();
        releasePlayer();
        preparePlayer(mPlayReady);
        player.setBackgrounded(backgrounded);
    }

    // Permission request listener method

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            preparePlayer(mPlayReady);
        } else {
            Toast.makeText(getContext(), R.string.storage_permission_denied, Toast.LENGTH_LONG).show();
            getActivity().finish();
        }
    }

    // Permission management methods

    /**
     * Checks whether it is necessary to ask for permission to read storage. If necessary, it also
     * requests permission.
     *
     * @return true if a permission request is made. False if it is not necessary.
     */
    @TargetApi(23)
    private boolean maybeRequestPermission() {
        if (requiresPermission(contentUri)) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            return true;
        } else {
            return false;
        }
    }

    @TargetApi(23)
    private boolean requiresPermission(Uri uri) {
        return Util.SDK_INT >= 23
                && Util.isLocalFileUri(uri)
                && getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED;
    }

    // Internal methods


    /**
     * Returns DemoPlayer.RendererBuilder for a given contentType
     *
     * @return DemoPlayer.RendererBuilder instance
     */
    private DemoPlayer.RendererBuilder getRendererBuilder(int contentType) {
        String userAgent = Util.getUserAgent(getActivity(), "ExoPlayerDemo");
        switch (contentType) {
            case Util.TYPE_SS:
                return new SmoothStreamingRendererBuilder(getContext(), userAgent, contentUri.toString(),
                        new SmoothStreamingTestMediaDrmCallback());
            case Util.TYPE_DASH:
                return new DashRendererBuilder(getContext(), userAgent, contentUri.toString(),
                        new WidevineTestMediaDrmCallback("", ""));
            case Util.TYPE_HLS:
                return new HlsRendererBuilder(getContext(), userAgent, contentUri.toString());
            case Util.TYPE_OTHER:
                return new ExtractorRendererBuilder(getContext(), userAgent, contentUri);
            default:
                throw new IllegalStateException("Unsupported type: " + contentType);
        }
    }

    /**
     * @param playWhenReady if true - video begin play when it is
     */
    private void preparePlayer(boolean playWhenReady) {
        if (player == null) {
            player = new DemoPlayer(getRendererBuilder(contentType));
            player.addListener(this);
            player.setMetadataListener(this);
            player.seekTo(playerPosition);
            playerNeedsPrepare = true;
            mediaController.setMediaPlayer(player.getPlayerControl());
            mediaController.setEnabled(true);
            eventLogger = new EventLogger();
            eventLogger.startSession();
            player.addListener(eventLogger);
            player.setInfoListener(eventLogger);
            player.setInternalErrorListener(eventLogger);
        }
        if (playerNeedsPrepare) {
            player.prepare();
            playerNeedsPrepare = false;
        }
        player.setSurface(surfaceView.getHolder().getSurface());
        player.setPlayWhenReady(playWhenReady);
    }

    private void releasePlayer() {
        if (player != null) {
            playerPosition = player.getCurrentPosition();
            player.removeListener(this);
            player.removeListener(eventLogger);
            player.setMetadataListener(null);
            player.setInfoListener(null);
            player.setInternalErrorListener(null);
            player.release();
            player = null;
            eventLogger.endSession();
            eventLogger = null;
        }
    }

    // DemoPlayer.Listener implementation

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == ExoPlayer.STATE_ENDED) {
            showControls();
        }
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
                updateActivity();
                break;
            default:
                text += "unknown";
                break;
        }
        progressBar.setVisibility(showProgress ? View.VISIBLE : View.GONE);
        Log.d(TAG, text);
    }

    @Override
    public void onError(Exception e) {
        String errorString = null;
        if (e instanceof UnsupportedDrmException) {
            // Special case DRM failures.
            UnsupportedDrmException unsupportedDrmException = (UnsupportedDrmException) e;
            errorString = getString(Util.SDK_INT < 18 ? R.string.error_drm_not_supported
                    : unsupportedDrmException.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
                    ? R.string.error_drm_unsupported_scheme : R.string.error_drm_unknown);
        } else if (e instanceof ExoPlaybackException && e.getCause() instanceof MediaCodecTrackRenderer.DecoderInitializationException) {
            // Special case for decoder initialization failures.
            MediaCodecTrackRenderer.DecoderInitializationException decoderInitializationException =
                    (MediaCodecTrackRenderer.DecoderInitializationException) e.getCause();
            if (decoderInitializationException.decoderName == null) {
                if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                    errorString = getString(R.string.error_querying_decoders);
                } else if (decoderInitializationException.secureDecoderRequired) {
                    errorString = getString(R.string.error_no_secure_decoder, decoderInitializationException.mimeType);
                } else {
                    errorString = getString(R.string.error_no_decoder, decoderInitializationException.mimeType);
                }
            } else {
                errorString = getString(R.string.error_instantiating_decoder, decoderInitializationException.decoderName);
            }
        }
        if (errorString == null) {
            errorString = e.getMessage();
        }
        if (errorString != null) {
            Toast.makeText(getContext(), errorString, Toast.LENGTH_LONG).show();
        }
        playerNeedsPrepare = true;
        showControls();
        updateActivity();
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthAspectRatio) {
        videoFrame.setAspectRatio(height == 0 ? 1 : (width * pixelWidthAspectRatio) / height);
    }

    // User controls

    private void toggleControlsVisibility() {
        if (mediaController.isShowing()) {
            hideControls();
        } else {
            showControls();
        }
    }

    private void showControls() {
        mediaController.show(0);
    }

    private void hideControls() {
        mediaController.hide();
    }

    // DemoPlayer.MetadataListener implementation

    @Override
    public void onId3Metadata(List<Id3Frame> id3Frames) {
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

    // SurfaceHolder.Callback implementation

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (player != null) {
            player.setSurface(holder.getSurface());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Do nothing.
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (player != null) {
            player.blockingClearSurface();
        }
    }

    /**
     * Notify activity that video is ready to play
     */
    private void updateActivity() {
        if (!mPlayReady) {
            Activity activity = getActivity();
            if (activity instanceof PlayerFragmentListener) {
                PlayerFragmentListener listener = (PlayerFragmentListener) activity;
                listener.movieLoaded();
            }
            mPlayReady = true;
        }
    }

    private final class KeyCompatibleMediaController extends MediaController {

        private MediaController.MediaPlayerControl playerControl;

        public KeyCompatibleMediaController(Context context) {
            super(context);
        }

        @Override
        public void setMediaPlayer(MediaController.MediaPlayerControl playerControl) {
            super.setMediaPlayer(playerControl);
            this.playerControl = playerControl;
        }

        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            int keyCode = event.getKeyCode();
            if (playerControl.canSeekForward() && keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    playerControl.seekTo(playerControl.getCurrentPosition() + 15000); // milliseconds
                    show();
                }
                return true;
            } else if (playerControl.canSeekBackward() && keyCode == KeyEvent.KEYCODE_MEDIA_REWIND) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    playerControl.seekTo(playerControl.getCurrentPosition() - 5000); // milliseconds
                    show();
                }
                return true;
            }
            return super.dispatchKeyEvent(event);
        }

        @Override
        public void show(int timeout) {
            super.show(timeout);
            showNavigation();
        }

        @Override
        public void hide() {
            super.hide();
            hideNavigation();
        }
    }

    public void showNavigation() {
        navigationLayout.setVisibility(View.VISIBLE);
    }

    public void hideNavigation() {
        navigationLayout.setVisibility(View.INVISIBLE);
    }

}
