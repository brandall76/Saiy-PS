/*
 * Copyright (c) 2016. Saiy Ltd. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ai.saiy.android.sound;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Pair;

import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

/**
 * Class to handle the manipulation of various system volume settings, including the ducking of
 * audio during speech and the pausing of audio during recognition.
 * <p/>
 * Unfortunately, many applications that are broadcasting media do not adhere to these requests.
 * <p/>
 * Static access for ease of use.
 * <p/>
 * Created by benrandall76@gmail.com on 26/03/2016.
 */
public class VolumeHelper {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = VolumeHelper.class.getSimpleName();

    private static final float VOLUME_MARGIN = 0.2F;

    /**
     * Our {@link AudioManager.OnAudioFocusChangeListener}
     */
    private static final AudioManager.OnAudioFocusChangeListener audioFocus = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(final int focusChange) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "AudioManager focusChange: " + focusChange);
            }

            switch (focusChange) {

                case AudioManager.AUDIOFOCUS_GAIN:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "AudioManager focusChange: AUDIOFOCUS_GAIN");
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "AudioManager focusChange: AUDIOFOCUS_LOSS");
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "AudioManager focusChange: AUDIOFOCUS_LOSS_TRANSIENT");
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "AudioManager focusChange: AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                    }
                    break;
                default:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "AudioManager focusChange: AUDIOFOCUS default");
                    }
                    break;
            }
        }
    };

    /**
     * Mute the ringtone during an incoming call. This method should be used with caution.
     * <p/>
     * The ringtone cannot be 'ducked' as with other media.
     *
     * @param ctx  the application context
     * @param mute whether or not to mute or restore the ringtone settings.
     */
    @SuppressWarnings("deprecation")
    public static void muteRinger(final Context ctx, final boolean mute) {
        if (DEBUG) {
            MyLog.d(CLS_NAME, "Ringer Muting: " + mute);
        }

        final AudioManager am = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);

        if (DEBUG) {
            switch (SPH.getDefaultRinger(ctx)) {

                case AudioManager.RINGER_MODE_NORMAL:
                    MyLog.v(CLS_NAME, "getRingerDefault: RINGER_MODE_NORMAL");
                    break;
                case AudioManager.RINGER_MODE_SILENT:
                    MyLog.v(CLS_NAME, "getRingerDefault: RINGER_MODE_SILENT");
                    break;
                case AudioManager.RINGER_MODE_VIBRATE:
                    MyLog.v(CLS_NAME, "getRingerDefault: RINGER_MODE_VIBRATE");
                    break;
                default:
                    MyLog.w(CLS_NAME, "getRingerDefault: " + SPH.getDefaultRinger(ctx));
                    break;
            }

            switch (am.getRingerMode()) {

                case AudioManager.RINGER_MODE_NORMAL:
                    MyLog.i(CLS_NAME, "getRingerMode: RINGER_MODE_NORMAL");
                    break;
                case AudioManager.RINGER_MODE_SILENT:
                    MyLog.i(CLS_NAME, "getRingerMode: RINGER_MODE_SILENT");
                    break;
                case AudioManager.RINGER_MODE_VIBRATE:
                    MyLog.i(CLS_NAME, "getRingerMode: RINGER_MODE_VIBRATE");
                    break;
                default:
                    MyLog.w(CLS_NAME, "getRingerMode: " + am.getRingerMode());
                    break;
            }

            switch (am.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER)) {

                case AudioManager.VIBRATE_SETTING_ON:
                    MyLog.i(CLS_NAME, "getVibrateSetting: VIBRATE_SETTING_ON");
                    break;
                case AudioManager.VIBRATE_SETTING_OFF:
                    MyLog.i(CLS_NAME, "getVibrateSetting: VIBRATE_SETTING_OFF");
                    break;
                case AudioManager.VIBRATE_SETTING_ONLY_SILENT:
                    MyLog.i(CLS_NAME, "getVibrateSetting: VIBRATE_SETTING_ONLY_SILENT");
                    break;
                default:
                    MyLog.w(CLS_NAME, "getVibrateSetting: Default");
                    break;
            }

            MyLog.i(CLS_NAME, "shouldVibrate: " + am.shouldVibrate(AudioManager.VIBRATE_TYPE_RINGER));
        }

        if (mute) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "Ringer Muting");
            }

            SPH.setDefaultRinger(ctx, am.getRingerMode());

            try {
                am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "Ringer Muting Exception");
                    e.printStackTrace();
                }
            }

        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "Ringer Muting Restore");
            }

            try {
                am.setRingerMode(SPH.getDefaultRinger(ctx));
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "Ringer Restore Exception");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Duck the audio
     *
     * @param ctx the application context
     */
    public static void duckAudioMedia(final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "duckAudioMedia");
        }

        try {

            final AudioManager audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);

            switch (audioManager.requestAudioFocus(audioFocus, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)) {

                case AudioManager.AUDIOFOCUS_REQUEST_FAILED:
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "AudioManager duckAudioMedia AUDIOFOCUS_REQUEST_FAILED");
                    }
                    break;
                case AudioManager.AUDIOFOCUS_REQUEST_GRANTED:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "AudioManager duckAudioMedia AUDIOFOCUS_REQUEST_GRANTED");
                    }
                    break;
                default:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "AudioManager duckAudioMedia AUDIOFOCUS default");
                    }
                    break;
            }
        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "duckAudioMedia: NullPointerException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "duckAudioMedia: Exception");
                e.printStackTrace();
            }
        }
    }


    /**
     * Pause the audio
     *
     * @param ctx the application context
     */
    public static void pauseAudioMedia(final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "pauseAudioMedia");
        }

        try {

            final AudioManager audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);

            int requestType;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                requestType = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE;
            } else {
                requestType = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT;
            }

            switch (audioManager.requestAudioFocus(audioFocus, AudioManager.STREAM_MUSIC, requestType)) {

                case AudioManager.AUDIOFOCUS_REQUEST_FAILED:
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "AudioManager pauseAudioMedia AUDIOFOCUS_REQUEST_FAILED");
                    }
                    break;
                case AudioManager.AUDIOFOCUS_REQUEST_GRANTED:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "AudioManager pauseAudioMedia AUDIOFOCUS_REQUEST_GRANTED");
                    }
                    break;
                default:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "AudioManager pauseAudioMedia AUDIOFOCUS default");
                    }
                    break;
            }
        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "abandonAudioMedia: NullPointerException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "abandonAudioMedia: Exception");
                e.printStackTrace();
            }
        }
    }

    /**
     * Notify the System that any previous condition requiring to duck or pause audio is now complete.
     *
     * @param ctx the application context
     */
    public static void abandonAudioMedia(final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "abandonAudioMedia");
        }

        try {

            final AudioManager audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);

            switch (audioManager.abandonAudioFocus(audioFocus)) {

                case AudioManager.AUDIOFOCUS_REQUEST_FAILED:
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "AudioManager abandonAudioMedia AUDIOFOCUS_REQUEST_FAILED");
                    }
                    break;
                case AudioManager.AUDIOFOCUS_REQUEST_GRANTED:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "AudioManager abandonAudioMedia AUDIOFOCUS_REQUEST_GRANTED");
                    }
                    break;
                default:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "AudioManager abandonAudioMedia AUDIOFOCUS default");
                    }
                    break;
            }
        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "abandonAudioMedia: NullPointerException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "abandonAudioMedia: Exception");
                e.printStackTrace();
            }
        }
    }

    /**
     * Get the value we should set the media stream to prior to speech, based on the current stream level
     * and the percentage by which the user prefers the stream to be adjusted
     *
     * @param ctx the application context
     * @return the rounded stream level
     */
    public static Pair<Boolean, Float> getUserMediaVolumeValue(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getUserMediaVolumeValue");
        }

        try {

            final AudioManager audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);

            final boolean volumeProfileEnabled = volumeProfileEnabled(audioManager);

            if (volumeProfileEnabled) {

                final int currentPercentage = getMediaVolumePercentage(audioManager);
                final int userPercentage = SPH.getTTSVolume(ctx);
                final int combinedPercentage = currentPercentage + userPercentage;

                if (currentPercentage > 0) {
                    if (combinedPercentage > 0) {
                        if (combinedPercentage > 100) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "getUserMediaVolumeValue: using combined max");
                            }
                            return new Pair<>(true, 1F);
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "getUserMediaVolumeValue: using combined");
                            }
                            return new Pair<>(true, ((float) combinedPercentage / 100));
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "getUserMediaVolumeValue: combined below zero: applying margin");
                        }
                        return new Pair<>(true, VOLUME_MARGIN);
                    }
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "getUserMediaVolumeValue: sound off");
                    }
                }
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getUserMediaVolumeValue: device muted");
                }
            }
        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getUserMediaVolumeValue: NullPointerException");
                e.printStackTrace();
            }
        } catch (final ArithmeticException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getUserMediaVolumeValue: ArithmeticException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getUserMediaVolumeValue: Exception");
                e.printStackTrace();
            }
        }

        return new Pair<>(false, 0F);
    }

    /**
     * Check if the device can currently output sound, by checking the media profile. This is unfortunately
     * not foolproof, due to the various ways different ROM and device manufacturers link the stream types.
     *
     * @param audioManager object
     * @return true if the device will output sounds, false otherwise
     */
    private static boolean volumeProfileEnabled(@NonNull final AudioManager audioManager) {

        switch (audioManager.getRingerMode()) {

            case AudioManager.RINGER_MODE_NORMAL:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "volumeProfileEnabled: RINGER_MODE_NORMAL");
                }
                return true;
            case AudioManager.RINGER_MODE_VIBRATE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "volumeProfileEnabled: RINGER_MODE_VIBRATE");
                }
                break;
            case AudioManager.RINGER_MODE_SILENT:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "volumeProfileEnabled: RINGER_MODE_SILENT");
                }
                break;
            default:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "volumeProfileEnabled: Default");
                }
                break;
        }

        return false;

    }

    /**
     * Get a media stream value based on a given percentage of the maximum permitted volume level
     *
     * @param audioManager object
     * @param percentage   to calculate
     * @return the rounded percentage
     */
    private static int getMediaPercentageValue(@NonNull final AudioManager audioManager, final int percentage) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getMediaPercentageValue");
        }

        final int maxVolume = getMaxMediaVolume(audioManager);
        final int resolvedValue = Math.round(maxVolume * percentage);

        if (DEBUG) {
            MyLog.i(CLS_NAME, "getMediaPercentageValue: percentage: " + percentage);
            MyLog.i(CLS_NAME, "getMediaPercentageValue: maxVolume: " + maxVolume);
            MyLog.i(CLS_NAME, "getMediaPercentageValue: resolvedValue: " + resolvedValue);
        }

        return resolvedValue;
    }

    /**
     * Get the percentage of the current media volume in relation to the maximum level permitted
     *
     * @param audioManager object
     * @return the integer value or {@link Integer#MAX_VALUE} if the request fails
     */
    private static int getMediaVolumePercentage(@NonNull final AudioManager audioManager) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getMediaVolumePercentage");
        }

        final int currentVolume = getMediaVolume(audioManager);
        final int maxVolume = getMaxMediaVolume(audioManager);
        final int percentage = (int) Math.round(100.0 * currentVolume / maxVolume);

        if (DEBUG) {
            MyLog.i(CLS_NAME, "getMediaVolumePercentage: currentVolume: " + currentVolume);
            MyLog.i(CLS_NAME, "getMediaVolumePercentage: maxVolume: " + maxVolume);
            MyLog.i(CLS_NAME, "getMediaVolumePercentage: percentage: " + percentage);
        }

        return percentage;
    }

    /**
     * Get the current volume value of the media stream
     *
     * @param audioManager object
     * @return the integer value
     */
    private static int getMediaVolume(@NonNull final AudioManager audioManager) {
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    /**
     * Get the current volume value of the media stream
     *
     * @param ctx the application context
     * @return the integer value
     */
    public static int getMediaVolume(@NonNull final Context ctx) {
        final AudioManager audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    /**
     * Get the maximum volume value of the media stream
     *
     * @param audioManager object
     * @return the integer value
     */
    private static int getMaxMediaVolume(@NonNull final AudioManager audioManager) {
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    /**
     * Get the maximum volume value of the media stream
     *
     * @param ctx the application context
     * @return the integer value
     */
    public static int getMaxMediaVolume(@NonNull final Context ctx) {
        final AudioManager audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }
}
