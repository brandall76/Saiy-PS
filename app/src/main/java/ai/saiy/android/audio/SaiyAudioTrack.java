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
import android.media.AudioManager;
import android.media.AudioTrack;
import android.support.annotation.NonNull;
import android.util.Pair;

import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import ai.saiy.android.tts.SaiyProgressListener;
import ai.saiy.android.utils.MyLog;

/**
 * Wrapper class around the {@link AudioTrack} object to handle application specific eventualities
 * <p>
 * Created by benrandall76@gmail.com on 28/04/2016.
 */
public class SaiyAudioTrack extends AudioTrack {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = SaiyAudioTrack.class.getSimpleName();

    private static final int WAV_OFFSET = 44;
    private static final int MAX_AUDIO_BUFFER_SIZE = 8192;
    private static final int SAMPLE_RATE_HZ = 16000;
    private static final int CHANNEL_OUT = AudioFormat.CHANNEL_OUT_MONO;
    private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private static final int MIN_BUFFER_SIZE = AudioTrack.getMinBufferSize(SAMPLE_RATE_HZ, CHANNEL_OUT, ENCODING);

    private volatile SaiyProgressListener listener;

    private final BlockingQueue<Pair<byte[], String>> byteQueue = new LinkedBlockingQueue<>();

    /**
     * Class constructor.
     *
     * @param streamType        the type of the audio stream. See
     *                          {@link AudioManager#STREAM_VOICE_CALL}, {@link AudioManager#STREAM_SYSTEM},
     *                          {@link AudioManager#STREAM_RING}, {@link AudioManager#STREAM_MUSIC},
     *                          {@link AudioManager#STREAM_ALARM}, and {@link AudioManager#STREAM_NOTIFICATION}.
     * @param sampleRateInHz    the initial source sample rate expressed in Hz.
     * @param channelConfig     describes the configuration of the audio channels.
     *                          See {@link AudioFormat#CHANNEL_OUT_MONO} and
     *                          {@link AudioFormat#CHANNEL_OUT_STEREO}
     * @param audioFormat       the format in which the audio data is represented.
     *                          See {@link AudioFormat#ENCODING_PCM_16BIT},
     *                          {@link AudioFormat#ENCODING_PCM_8BIT},
     *                          and {@link AudioFormat#ENCODING_PCM_FLOAT}.
     * @param bufferSizeInBytes the total size (in bytes) of the internal buffer where audio data is
     *                          read from for playback. This should be a multiple of the frame size in bytes.
     *                          <p> If the track's creation mode is {@link #MODE_STATIC},
     *                          this is the maximum length sample, or audio clip, that can be played by this instance.
     *                          <p> If the track's creation mode is {@link #MODE_STREAM},
     *                          this should be the desired buffer size
     *                          for the <code>AudioTrack</code> to satisfy the application's
     *                          natural latency requirements.
     *                          If <code>bufferSizeInBytes</code> is less than the
     *                          minimum buffer size for the output sink, it is automatically increased to the minimum
     *                          buffer size.
     *                          The method {@link #getBufferSizeInFrames()} returns the
     *                          actual size in frames of the native buffer created, which
     *                          determines the frequency to write
     *                          to the streaming <code>AudioTrack</code> to avoid under-run.
     * @param mode              streaming or static buffer. See {@link #MODE_STATIC} and {@link #MODE_STREAM}
     * @throws java.lang.IllegalArgumentException
     */
    public SaiyAudioTrack(final int streamType, final int sampleRateInHz, final int channelConfig,
                          final int audioFormat, final int bufferSizeInBytes, final int mode) throws IllegalArgumentException {
        super(streamType, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes, mode);
    }

    @Override
    public int write(@NonNull final byte[] audioData, final int offsetInBytes, final int sizeInBytes) {
        return super.write(audioData, offsetInBytes, sizeInBytes);
    }

    /**
     * Stops playing the audio data.
     * When used on an instance created in {@link #MODE_STREAM} mode, audio will stop playing
     * after the last buffer that was written has been played. For an immediate stop, use
     * {@link #pause()}, followed by {@link #flush()} to discard audio data that hasn't been played
     * back yet.
     *
     * @throws IllegalStateException
     */
    @Override
    public void stop() throws IllegalStateException {
        super.stop();
    }

    /**
     * Handles the two circumstances of stop being called at the end of standard playback, or an
     * interrupt call, where the queue needs to be cleared. In the case that the queue needs to be
     * cleared, an {@link IllegalStateException} is likely to be thrown, but handled gracefully
     * in {@link #enqueue(byte[], String)} and here.
     *
     * @param interrupt true if all pending audio should be stopped.
     */
    public void stop(final boolean interrupt) {
        if (interrupt) {
            byteQueue.clear();

            flush();
            release();
        }

        try {
            super.stop();
        } catch (final IllegalStateException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "stop: IllegalStateException");
                e.printStackTrace();
            }
        }
    }

    /**
     * Listener to notify of audio callbacks
     *
     * @param listener the {@link SaiyProgressListener}
     */
    public void setListener(@NonNull final SaiyProgressListener listener) {
        this.listener = listener;
    }

    /**
     * Add a byte[] of uncompressed audio to the queue to process. If the queue isn't currently
     * processing any audio, it will be started.
     *
     * @param uncompressedBytes the uncompressed audio byte[]
     * @param utteranceId       the utterance id
     */
    public void enqueue(@NonNull final byte[] uncompressedBytes, @NonNull final String utteranceId) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "enqueue: queue size: " + byteQueue.size());
        }

        synchronized (byteQueue) {
            if (byteQueue.isEmpty()) {
                byteQueue.add(new Pair<>(uncompressedBytes, utteranceId));
                try {
                    process();
                } catch (final NoSuchElementException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "enqueue: process: NoSuchElementException");
                        e.printStackTrace();
                    }
                } catch (final IllegalStateException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "enqueue: process: IllegalStateException");
                        e.printStackTrace();
                    }
                }
            } else {
                byteQueue.add(new Pair<>(uncompressedBytes, utteranceId));
            }
        }
    }

    /**
     * Process any pending audio
     */
    private synchronized void process() throws NoSuchElementException, IllegalStateException {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "process");
        }

        while (!byteQueue.isEmpty()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "processing: queue size: " + byteQueue.size());
            }

            play();
            listener.onStart(byteQueue.element().second);

            int offset = SaiyAudioTrack.WAV_OFFSET;
            int bytesToWrite;
            String utteranceId = null;
            while (byteQueue.element() != null && offset < byteQueue.element().first.length) {

                utteranceId = byteQueue.element().second;
                bytesToWrite = Math.min(SaiyAudioTrack.MAX_AUDIO_BUFFER_SIZE,
                        byteQueue.element().first.length - offset);
                write(byteQueue.element().first, offset, bytesToWrite);
                offset += bytesToWrite;
            }

            byteQueue.remove();

            if (byteQueue.isEmpty()) {
                stop(false);
            }
            listener.onDone(utteranceId);
        }

        if (DEBUG) {
            MyLog.i(CLS_NAME, "processing complete. Queue empty");
        }
    }

    /**
     * Static helper method to create a {@link SaiyAudioTrack} object, as the Constructor parameters
     * will always be the same.
     *
     * @return a new {@link SaiyAudioTrack} object
     */
    public static SaiyAudioTrack getSaiyAudioTrack() {

        try {
            return new SaiyAudioTrack(AudioManager.STREAM_MUSIC, SaiyAudioTrack.SAMPLE_RATE_HZ,
                    SaiyAudioTrack.CHANNEL_OUT, SaiyAudioTrack.ENCODING, SaiyAudioTrack.MIN_BUFFER_SIZE,
                    SaiyAudioTrack.MODE_STREAM);
        } catch (final IllegalArgumentException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getSaiyAudioTrack: IllegalArgumentException");
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * Static helper method to create a {@link SaiyAudioTrack} object.
     *
     * @param stream the stream type
     * @return a new {@link SaiyAudioTrack} object
     */
    public static SaiyAudioTrack getSaiyAudioTrack(final int stream) {

        try {
            return new SaiyAudioTrack(stream, SaiyAudioTrack.SAMPLE_RATE_HZ,
                    SaiyAudioTrack.CHANNEL_OUT, SaiyAudioTrack.ENCODING, SaiyAudioTrack.MIN_BUFFER_SIZE,
                    SaiyAudioTrack.MODE_STREAM);
        } catch (final IllegalArgumentException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getSaiyAudioTrack: IllegalArgumentException");
                e.printStackTrace();
            }
            return null;
        }
    }

    public boolean streamMatches(final int stream) {
        return stream == super.getStreamType();
    }
}
