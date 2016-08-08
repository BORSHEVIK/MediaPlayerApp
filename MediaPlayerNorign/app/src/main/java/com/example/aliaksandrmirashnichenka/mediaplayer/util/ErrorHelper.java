package com.example.aliaksandrmirashnichenka.mediaplayer.util;

import android.content.Context;
import android.support.annotation.NonNull;

import com.example.aliaksandrmirashnichenka.mediaplayer.R;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.MediaCodecUtil;
import com.google.android.exoplayer.drm.UnsupportedDrmException;
import com.google.android.exoplayer.util.Util;

/**
 * This is class for help with work with errors handling
 *
 * Created by aliaksandrmirashnichenka on 08.08.16.
 */
public class ErrorHelper {


    /**
     *
     * @param e - current exception
     * @param context - root context
     * @return - message with information about error. can be Null
     */
    public static String getBuildMessageFromError(@NonNull Exception e, @NonNull Context context) {
        String message = null;

        if (e instanceof UnsupportedDrmException) {
            // Special case DRM failures.
            UnsupportedDrmException unsupportedDrmException = (UnsupportedDrmException) e;
            message = context.getString(Util.SDK_INT < 18 ? R.string.error_drm_not_supported
                    : unsupportedDrmException.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
                    ? R.string.error_drm_unsupported_scheme : R.string.error_drm_unknown);
        } else if (e instanceof ExoPlaybackException && e.getCause() instanceof MediaCodecTrackRenderer.DecoderInitializationException) {
            // Special case for decoder initialization failures.
            MediaCodecTrackRenderer.DecoderInitializationException decoderInitializationException =
                    (MediaCodecTrackRenderer.DecoderInitializationException) e.getCause();
            if (decoderInitializationException.decoderName == null) {
                if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                    message = context.getString(R.string.error_querying_decoders);
                } else if (decoderInitializationException.secureDecoderRequired) {
                    message = context.getString(R.string.error_no_secure_decoder, decoderInitializationException.mimeType);
                } else {
                    message = context.getString(R.string.error_no_decoder, decoderInitializationException.mimeType);
                }
            } else {
                message = context.getString(R.string.error_instantiating_decoder, decoderInitializationException.decoderName);
            }
        }

        return message;
    }

}
