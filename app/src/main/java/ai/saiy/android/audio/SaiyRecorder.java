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
import android.os.Looper;
import android.support.annotation.NonNull;

import ai.saiy.android.utils.MyLog;

/**
 * Wrapper around the {@link AudioRecord class} to handle errors and setup.
 * <p/>
 * Note - currently only configured for uncompressed recordings.
 * <p/>
 * Created by benrandall76@gmail.com on 15/02/2016.
 */
public class SaiyRecorder {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = SaiyRecorder.class.getSimpleName();

    private SaiyAudio saiyAudio;

    // Will use in future compressed recordings
    private final short nChannels = 1;
    private final short bSamples = 16;
    private final int TIMER_INTERVAL = 120;
    private int framePeriod;

    private final int audioSource;
    private final int sampleRateInHz;
    private final int channelConfig;
    private final int audioFormat;
    private final int bufferSizeInBytes;
    private final boolean enhance;

    /**
     * Constructor
     * <p>
     * Uses the most common application defaults
     */
    public SaiyRecorder() {
        this.audioSource = MediaRecorder.AudioSource.VOICE_RECOGNITION;
        this.sampleRateInHz = 8000;
        this.channelConfig = AudioFormat.CHANNEL_IN_MONO;
        this.audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        this.bufferSizeInBytes = calculateBufferSize();
        this.enhance = true;
    }

    /**
     * Constructor
     *
     * @param audioSource    the audio source
     * @param sampleRateInHz the sampling rate in hertz
     * @param channelConfig  the channel configuration
     * @param audioFormat    the audio format
     */
    public SaiyRecorder(final int audioSource, final int sampleRateInHz, final int channelConfig,
                        final int audioFormat, final boolean enhance) {
        this.audioSource = audioSource;
        this.sampleRateInHz = sampleRateInHz;
        this.channelConfig = channelConfig;
        this.audioFormat = audioFormat;
        this.bufferSizeInBytes = calculateBufferSize();
        this.enhance = enhance;
    }

    /**
     * Constructor
     *
     * @param audioSource       the audio source
     * @param sampleRateInHz    the sampling rate in hertz
     * @param channelConfig     the channel configuration
     * @param audioFormat       the audio format
     * @param bufferSizeInBytes the buffer size in bytes
     */
    public SaiyRecorder(final int audioSource, final int sampleRateInHz, final int channelConfig,
                        final int audioFormat, final int bufferSizeInBytes, final boolean enhance) {
        this.audioSource = audioSource;
        this.sampleRateInHz = sampleRateInHz;
        this.channelConfig = channelConfig;
        this.audioFormat = audioFormat;
        this.bufferSizeInBytes = bufferSizeInBytes;
        this.enhance = enhance;
    }

    /**
     * Initialise the Voice Recorder
     *
     * @return The audio record initialisation state.
     */
    public int initialise() {

        int count = 0;

        while (count < 4) {
            count++;

            saiyAudio = new SaiyAudio(audioSource, sampleRateInHz, channelConfig, audioFormat,
                    bufferSizeInBytes, enhance);

            if (saiyAudio.getState() == AudioRecord.STATE_INITIALIZED) {
                return AudioRecord.STATE_INITIALIZED;
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "SaiyAudio reinitialisation attempt ~ " + count);
                }

                if (Looper.myLooper() != null && Looper.myLooper() != Looper.getMainLooper()) {

                    // Give the audio object a small chance to sort itself out
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "SaiyAudio InterruptedException");
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        if (DEBUG) {
            MyLog.w(CLS_NAME, "SaiyAudio initialisation failed");
        }

        return AudioRecord.STATE_UNINITIALIZED;
    }


    /**
     * Reads audio data from the audio hardware for recording into a byte array.
     *
     * @param buffer the array to which the recorded audio data is written.
     * @return the number of bytes that were read. The number of bytes will not exceed sizeInBytes.
     */
    public int read(@NonNull final byte[] buffer) {
        return saiyAudio.read(buffer, 0, buffer.length);
    }

    /**
     * Reads audio data from the audio hardware for recording into a short array.
     *
     * @param audioData short audio data
     * @param offsetInShorts the offset to read from
     * @param sizeInShorts size of the audio data
     * @return size read
     */
    public int read(short[] audioData, int offsetInShorts, int sizeInShorts) {
        return saiyAudio.read(audioData, offsetInShorts, sizeInShorts);
    }

    /**
     * Get the recording state from the audio session.
     *
     * @return the audio session state
     */
    public int getRecordingState() {
        return saiyAudio.getRecordingState();
    }

    public int getBufferSize() {
        return bufferSizeInBytes;
    }

    /**
     * Start the audio recording session.
     */
    public int startRecording() {

        try {

            saiyAudio.startRecording();

            if (saiyAudio.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "AudioRecord == RECORDSTATE_RECORDING");
                }

                return AudioRecord.RECORDSTATE_RECORDING;

            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "AudioRecord != RECORDSTATE_RECORDING");
                }
            }

        } catch (final IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "AudioRecord IllegalStateException");
                e.printStackTrace();
            }
        } catch (NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "AudioRecord NullPointerException");
                e.printStackTrace();
            }
        }

        return AudioRecord.ERROR;
    }

    /**
     * Shutdown the audio session
     *
     * @param from a String label to identify the source of the request. Useful for logging only.
     */
    public void shutdown(@NonNull final String from) {

        if (saiyAudio == null) {
            return;
        }

        if (saiyAudio.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {

            try {
                saiyAudio.stop();
            } catch (final IllegalStateException e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "saiyAudio.stop(): IllegalStateException ~ " + from);
                }
            } catch (final NullPointerException e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "saiyAudio.stop(): NullPointerException ~ " + from);
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "saiyAudio.stop(): Exception ~ " + from);
                }
            }

        } else {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "saiyAudio != AudioRecord.RECORDSTATE_RECORDING ~ " + from);
            }
        }

        try {
            saiyAudio.release();
        } catch (final IllegalStateException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "saiyAudio.release(): IllegalStateException ~ " + from);
            }
        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "saiyAudio.release(): NullPointerException ~ " + from);
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "saiyAudio.release(): Exception ~ " + from);
            }
        }

        if (DEBUG) {
            MyLog.d(CLS_NAME, "saiyAudio set to NULL ~ " + from);
        }

        saiyAudio = null;
    }

    /**
     * Calculate the buffer size.
     *
     * @return the calculated buffer size
     */
    private int calculateBufferSize() {

        framePeriod = sampleRateInHz * TIMER_INTERVAL / 1000;
        int bufferSize = framePeriod * 2 * bSamples * nChannels / 8;

        if (DEBUG) {
            MyLog.i(CLS_NAME, "bufferSize: " + bufferSize);
        }

        final int minBuff = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);

        switch (minBuff) {

            case AudioRecord.ERROR:
            case AudioRecord.ERROR_BAD_VALUE:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "AudioRecord.ERROR/ERROR_BAD_VALUE");
                }
                break;
            default:

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "minBuff: " + minBuff);
                }

                if (bufferSize < minBuff) {
                    bufferSize = minBuff;

                    // Unused for now
                    framePeriod = bufferSize / (2 * bSamples * nChannels / 8);
                }

                break;
        }


        if (DEBUG) {
            MyLog.i(CLS_NAME, "bufferSize returning: " + bufferSize);
        }

        return bufferSize;
    }
}

