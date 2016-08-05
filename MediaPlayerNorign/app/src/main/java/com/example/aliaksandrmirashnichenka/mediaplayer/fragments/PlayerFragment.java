package com.example.aliaksandrmirashnichenka.mediaplayer.fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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

import com.example.aliaksandrmirashnichenka.mediaplayer.managers.MediaPlayerManager;
import com.example.aliaksandrmirashnichenka.mediaplayer.managers.MediaPlayerManagerListener;
import com.example.aliaksandrmirashnichenka.mediaplayer.R;
import com.example.aliaksandrmirashnichenka.mediaplayer.models.Movie;
import com.example.aliaksandrmirashnichenka.mediaplayer.util.ImageHelper;
import com.google.android.exoplayer.AspectRatioFrameLayout;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
import com.google.android.exoplayer.util.Util;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is fragment which showing video
 *
 * Created by aliaksandrmirashnichenka on 01.08.16.
 */
@EFragment(R.layout.fragment_video)
public class PlayerFragment extends Fragment  implements  SurfaceHolder.Callback, AudioCapabilitiesReceiver.Listener,
        PlayerFragmentViewsListener, MediaPlayerManagerListener {

    @SuppressWarnings("unused")
    private static final String TAG = PlayerFragment.class.getSimpleName();

    private static final int PLAYER_CONTROLL_ADDITIONAL_POSITION_UP   = 15000; //milliseconds
    private static final int PLAYER_CONTROLL_ADDITIONAL_POSITION_DOWN = 5000;  //milliseconds

    private static final CookieManager sDefaultCookieManager;

    static {
        sDefaultCookieManager = new CookieManager();
        sDefaultCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private Uri mContentUri;

    private int mContentType;

    private AudioCapabilitiesReceiver mAudioCapabilitiesReceiver;

    @Bean
    protected MediaPlayerManager mMediaPlayerManager;

    private MediaController mMediaController;

    @ViewById(R.id.video_frame)
    protected AspectRatioFrameLayout mVideoFrame;

    @ViewById(R.id.surface_view)
    protected SurfaceView mSurfaceView;

    @ViewById(R.id.navigation_panel)
    protected LinearLayout mNavigationLayout;

    @ViewById(R.id.progress)
    protected ProgressBar mProgressBar;

    @ViewById(R.id.root)
    protected View mRoot;

    @InstanceState
    protected ArrayList<Movie> mMovies;

    @InstanceState
    protected Movie mCurrentMovie;

    public void initInstance(@NonNull Movie movie,@NonNull ArrayList<Movie> movies) {
        mMovies = movies;
        mCurrentMovie = movie;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @AfterViews
    protected void initView() {

        mMediaPlayerManager.initMediaPlayerManager(this, this);

        if (mMediaPlayerManager.getPlayerReadyState()) {
            mMediaPlayerManager.setPlayerReadyState(MediaPlayerManager.NOT_PLAYER_WHEN_READY);
        }

        mRoot.setOnTouchListener((view1, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                toggleControlsVisibility();
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                view1.performClick();
            }
            return true;
        });
        mRoot.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE
                    || keyCode == KeyEvent.KEYCODE_MENU) {
                return false;
            } else {
                return mMediaController.dispatchKeyEvent(event);
            }
        });

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        mVideoFrame.setAspectRatio(1f * size.x / size.y);

        mSurfaceView.getHolder().addCallback(PlayerFragment.this);

        mMediaController = new KeyCompatibleMediaController(getContext());
        mMediaController.setAnchorView(mRoot);

        CookieHandler currentHandler = CookieHandler.getDefault();
        if (currentHandler != sDefaultCookieManager) {
            CookieHandler.setDefault(sDefaultCookieManager);
        }

        mAudioCapabilitiesReceiver = new AudioCapabilitiesReceiver(getContext(), PlayerFragment.this);
        mAudioCapabilitiesReceiver.register();

        List<Movie> movieList = mMovies;

        for (int i = 0; i < movieList.size(); i++) {
            final Movie movie = movieList.get(i);
            final ImageView imageView = (ImageView) mNavigationLayout.getChildAt(i);

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
                    toggleControlsVisibility();
                }
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

        if (Util.SDK_INT <= 23 || mMediaPlayerManager.getPlayer() == null) {
            onShown();
        }
    }

    /**
     * Set flag to player to play the video when it will be ready
     */
    public void playWhenReady() {
        mMediaPlayerManager.setPlayerReadyState(MediaPlayerManager.PLAYER_WHEN_READ);
        mMediaPlayerManager.preparePlayer(mContentType);
    }

    /**
     * Get movie instance from arguments and initialize the player
     */
    private void onShown() {
        Movie movie = mCurrentMovie;
        mContentUri = Uri.parse(movie.getStreams().getUrl());
        mMediaPlayerManager.setContentUri(mContentUri);
        // Use only this type
        mContentType = Util.TYPE_OTHER;

        if (mMediaPlayerManager.getPlayer() == null) {
            if (!maybeRequestPermission()) {
                mMediaPlayerManager.preparePlayer(mContentType);
            }
        } else {
            mMediaPlayerManager.getPlayer().setBackgrounded(false);
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
        mMediaPlayerManager.releasePlayer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mAudioCapabilitiesReceiver.unregister();
        mMediaPlayerManager.releasePlayer();
    }

    // AudioCapabilitiesReceiver.Listener methods

    @Override
    public void onAudioCapabilitiesChanged(@NonNull AudioCapabilities audioCapabilities) {
        if (mMediaPlayerManager.getPlayer() == null) {
            return;
        }
        boolean backgrounded = mMediaPlayerManager.getPlayer().getBackgrounded();
        mMediaPlayerManager.releasePlayer();
        mMediaPlayerManager.preparePlayer(mContentType);
        mMediaPlayerManager.getPlayer().setBackgrounded(backgrounded);
    }

    //SurfaceHolder.Callback implementation

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        if (mMediaPlayerManager.getPlayer() != null) {
            mMediaPlayerManager.getPlayer().setSurface(holder.getSurface());
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder,
                               int i,
                               int i1,
                               int i2) {
        // Do nothing.
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        if (mMediaPlayerManager.getPlayer() != null) {
            mMediaPlayerManager.getPlayer().blockingClearSurface();
        }
    }

    // Permission request listener method

    @Override
    public void onRequestPermissionsResult(@NonNull int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mMediaPlayerManager.preparePlayer(mContentType);
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
        if (requiresPermission(mContentUri)) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            return true;
        } else {
            return false;
        }
    }

    @TargetApi(23)
    private boolean requiresPermission(@NonNull Uri uri) {
        return Util.SDK_INT >= 23
                && Util.isLocalFileUri(uri)
                && getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED;
    }

    // User controls

    private void toggleControlsVisibility() {
        if (mMediaController.isShowing()) {
            hideControls();
        } else {
            showControls();
        }
    }

    private void showControls() {
        mMediaController.show(0);
    }

    private void hideControls() {
        mMediaController.hide();
    }

    /**
     * Notify activity that video is ready to play
     */
    private void updateActivity() {
        if (mMediaPlayerManager.getPlayerReadyState()) {
            Activity activity = getActivity();
            if (activity instanceof PlayerFragmentListener) {
                PlayerFragmentListener listener = (PlayerFragmentListener) activity;
                listener.movieLoaded();
            }
            mMediaPlayerManager.setPlayerReadyState(MediaPlayerManager.PLAYER_WHEN_READ);
        }
    }

    //PlayerFragmentViewsListener implementation

    @Override
    public MediaController getMediaController() {
        return mMediaController;
    }

    @Override
    public ProgressBar getProgressBar() {
        return mProgressBar;
    }

    @Override
    public AspectRatioFrameLayout getFrameLayout() {
        return mVideoFrame;
    }

    @Override
    public SurfaceView getSufraceView() {
        return mSurfaceView;
    }

    @Override
    public Activity getRootActivity() {
        return getActivity();
    }

    @Override
    public Context getRootContenxt() {
        return getContext();
    }

    //MediaPlayerManagerListener implementation

    @Override
    public void needUpdateActivity() {
        updateActivity();
    }

    @Override
    public void needShowControls() {
        showControls();
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
                    playerControl.seekTo(playerControl.getCurrentPosition() + PLAYER_CONTROLL_ADDITIONAL_POSITION_UP); // milliseconds
                    show();
                }
                return true;
            } else if (playerControl.canSeekBackward() && keyCode == KeyEvent.KEYCODE_MEDIA_REWIND) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    playerControl.seekTo(playerControl.getCurrentPosition() - PLAYER_CONTROLL_ADDITIONAL_POSITION_DOWN); // milliseconds
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

    private void showNavigation() {
        mNavigationLayout.setVisibility(View.VISIBLE);
    }

    private void hideNavigation() {
        mNavigationLayout.setVisibility(View.INVISIBLE);
    }

}
