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

package ai.saiy.android.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.NoiseSuppressor;
import android.os.Build;

import ai.saiy.android.utils.MyLog;

/**
 * Wrapper around {@link AudioRecord} to set enhancers by default if they are available.
 * <p/>
 * Created by benrandall76@gmail.com on 12/02/2016.
 */
public class SaiyAudio extends AudioRecord {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = SaiyAudio.class.getSimpleName();

    /**
     * Class constructor.
     * Though some invalid parameters will result in an {@link IllegalArgumentException} exception,
     * other errors do not.  Thus you should call {@link #getState()} immediately after construction
     * to confirm that the object is usable.
     *
     * @param audioSource       the recording source.
     *                          See {@link MediaRecorder.AudioSource} for the recording source definitions.
     * @param sampleRateInHz    the sample rate expressed in Hertz. 44100Hz is currently the only
     *                          rate that is guaranteed to work on all devices, but other rates such as 22050,
     *                          16000, and 11025 may work on some devices.
     * @param channelConfig     describes the configuration of the audio channels.
     *                          See {@link AudioFormat#CHANNEL_IN_MONO} and
     *                          {@link AudioFormat#CHANNEL_IN_STEREO}.  {@link AudioFormat#CHANNEL_IN_MONO} is guaranteed
     *                          to work on all devices.
     * @param audioFormat       the format in which the audio data is to be returned.
     *                          See {@link AudioFormat#ENCODING_PCM_8BIT}, {@link AudioFormat#ENCODING_PCM_16BIT},
     *                          and {@link AudioFormat#ENCODING_PCM_FLOAT}.
     * @param bufferSizeInBytes the total size (in bytes) of the buffer where audio data is written
     *                          to during the recording. New audio data can be read from this buffer in smaller chunks
     *                          than this size. See {@link #getMinBufferSize(int, int, int)} to determine the minimum
     *                          required buffer size for the successful creation of an AudioRecord instance. Using values
     *                          smaller than getMinBufferSize() will result in an initialization failure.
     * @param enhance           if audio enhancers should be added
     */
    public SaiyAudio(final int audioSource, final int sampleRateInHz, final int channelConfig,
                     final int audioFormat, final int bufferSizeInBytes, final boolean enhance)
            throws IllegalArgumentException {

        super(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);

        if (enhance && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "attempting audio enhancements");
            }

            setEnhancers(getAudioSessionId());
        }
    }

    /**
     * Attempt to set enhancers available on modern devices.
     * <p/>
     * These are hardware dependent, not build version. Although the APIs weren't available to
     * devices until API Level 16
     */
    @SuppressWarnings("NewApi")
    private void setEnhancers(final int sessionId) {

        if (!DEBUG) {
            NoiseSuppressor.create(sessionId);
            AcousticEchoCanceler.create(sessionId);
            AutomaticGainControl.create(sessionId);
        } else {
            if (NoiseSuppressor.create(sessionId) == null) {
                MyLog.i(CLS_NAME, "NoiseSuppressor null");
            } else {
                MyLog.i(CLS_NAME, "NoiseSuppressor success");
            }

            if (AcousticEchoCanceler.create(sessionId) == null) {
                MyLog.i(CLS_NAME, "AcousticEchoCanceler null");
            } else {
                MyLog.i(CLS_NAME, "AcousticEchoCanceler success");
            }

            if (AutomaticGainControl.create(sessionId) == null) {
                MyLog.i(CLS_NAME, "AutomaticGainControl null");
            } else {
                MyLog.i(CLS_NAME, "AutomaticGainControl success");
            }
        }
    }
}
