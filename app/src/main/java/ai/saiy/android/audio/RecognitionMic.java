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

import android.content.Context;
import android.media.AudioRecord;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import ai.saiy.android.audio.pause.PauseDetector;
import ai.saiy.android.audio.pause.PauseListener;
import ai.saiy.android.files.FileCreator;
import ai.saiy.android.recognition.Recognition;
import ai.saiy.android.recognition.SaiyRecognitionListener;
import ai.saiy.android.utils.MyLog;

/**
 * Created by benrandall76@gmail.com on 12/08/2016.
 */

public class RecognitionMic implements PauseListener {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = RecognitionMic.class.getSimpleName();

    public static final int SAMPLE_RATE_HZ_16000 = 16000;
    public static final int SAMPLE_RATE_HZ_8000 = 8000;

    private static final int ON_READY_FOR_SPEECH = 0;
    private static final int ON_BEGINNING_OF_SPEECH = 1;
    private static final int ON_ERROR = 2;
    private static final int ON_END_OF_SPEECH = 3;

    private final AtomicBoolean isRecording = new AtomicBoolean();
    private final AtomicBoolean isAvailable = new AtomicBoolean(true);
    private final AtomicBoolean isInterrupted = new AtomicBoolean();
    private final AtomicBoolean thrown = new AtomicBoolean();
    private final SaiySoundPool ssp;

    private FileCreator fileCreator;
    private PauseDetector pauseDetector;
    private volatile SaiyRecorder saiyRecorder;
    private final Object audioLock = new Object();
    private final Object errorLock = new Object();

    private final SaiyRecognitionListener listener;
    private final boolean pauseDetection;
    private final AtomicBoolean writeToFile;
    private IMic iMic;
    private final Object lock = new Object();
    private final Context mContext;

    public RecognitionMic(@NonNull final Context mContext, @Nullable final SaiyRecognitionListener listener,
                          @NonNull final AudioParameters audioParameters, final boolean pauseDetection,
                          final long pauseIgnoreTime, final boolean enhance, final boolean writeToFile,
                          @NonNull final SaiySoundPool ssp) {
        this.mContext = mContext;
        this.listener = listener;
        this.pauseDetection = pauseDetection;
        this.writeToFile = new AtomicBoolean(writeToFile);
        this.ssp = ssp;

        if (this.pauseDetection) {
            pauseDetector = new PauseDetector(this, audioParameters.getSampleRateInHz(),
                    audioParameters.getnChannels(), pauseIgnoreTime);
        }

        saiyRecorder = new SaiyRecorder(audioParameters.getAudioSource(),
                audioParameters.getSampleRateInHz(), audioParameters.getChannelConfig(),
                audioParameters.getAudioFormat(), enhance);

        if (this.writeToFile.get()) {
            fileCreator = new FileCreator(this.mContext, audioParameters.getnChannels(),
                    audioParameters.getSampleRateInHz(), audioParameters.getbSamples());
        }
    }

    public File getFile() {
        return fileCreator.getDefaultFile();
    }

    public void setMicListener(@NonNull final IMic iMic) {
        this.iMic = iMic;
    }

    public IMic getMicListener() {
        return iMic;
    }

    public Object getLock() {
        return lock;
    }

    public boolean isRecording() {
        return isRecording.get();
    }

    public boolean isAvailable() {
        return isAvailable.get();
    }

    public boolean isInterrupted() {
        return isInterrupted.get();
    }

    public SaiyRecognitionListener getRecognitionListener() {
        return listener;
    }

    public Context getContext() {
        return this.mContext;
    }

    public void stopRecording() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "stopRecording");
        }

        if (isRecording.get()) {
            isInterrupted.set(true);
            isRecording.set(false);
            Recognition.setState(Recognition.State.IDLE);
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "stopRecording: not recording");
            }
        }
    }

    @Override
    public void onPauseDetected() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onPauseDetected");
        }

        if (isRecording.get()) {
            isRecording.set(false);
            Recognition.setState(Recognition.State.IDLE);
            iMic.onPauseDetected();
        }
    }

    public void startRecording() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "startRecording");
        }

        isRecording.set(true);

        if (iMic == null) {
            throw new IllegalArgumentException("No iMic listener is set");
        }

        new Thread() {

            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

                ssp.play(ssp.getBeepStart());

                if (pauseDetection) {
                    pauseDetector.begin();
                }

                final int bufferSize = saiyRecorder.getBufferSize();

                final byte[] buffer = new byte[bufferSize];

                switch (saiyRecorder.initialise()) {

                    case AudioRecord.STATE_INITIALIZED:

                        try {

                            switch (saiyRecorder.startRecording()) {

                                case AudioRecord.RECORDSTATE_RECORDING:
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "AudioRecord.RECORDSTATE_RECORDING");
                                    }

                                    int count = 0;
                                    while (isRecording.get() && saiyRecorder != null
                                            && saiyRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {

                                        if (count == 0) {
                                            if (DEBUG) {
                                                MyLog.i(CLS_NAME, "Recording Started");
                                            }

                                            Recognition.setState(Recognition.State.LISTENING);
                                            recognitionListenerAction(ON_READY_FOR_SPEECH);
                                            iMic.onRecordingStarted();
                                            count++;
                                        }

                                        final int bufferReadResult = saiyRecorder.read(buffer);

                                        if (pauseDetection && !pauseDetector.hasDetected()) {
                                            pauseDetector.addLength(buffer, bufferReadResult);
                                            pauseDetector.monitor();
                                        }

                                        if (writeToFile.get()) {
                                            fileCreator.passBuffer(buffer);
                                        }

                                        iMic.onBufferReceived(bufferReadResult, buffer);
                                    }

                                    break;
                                case AudioRecord.ERROR:
                                default:
                                    if (DEBUG) {
                                        MyLog.w(CLS_NAME, "AudioRecord.ERROR");
                                    }
                                    onError(SpeechRecognizer.ERROR_AUDIO);
                                    break;
                            }

                        } catch (final IllegalStateException e) {
                            if (DEBUG) {
                                MyLog.e(CLS_NAME, "IllegalStateException");
                                e.printStackTrace();
                            }
                        } catch (final NullPointerException e) {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "NullPointerException");
                                e.printStackTrace();
                            }
                        } catch (final Exception e) {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "Exception");
                                e.printStackTrace();
                            }
                        }

                        audioShutdown();
                        break;

                    case AudioRecord.STATE_UNINITIALIZED:
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "AudioRecord.STATE_UNINITIALIZED");
                        }
                        onError(SpeechRecognizer.ERROR_AUDIO);
                        break;
                }
            }
        }.start();
    }

    private void onError(final int error) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onError");
        }

        synchronized (errorLock) {
            if (!thrown.get()) {
                thrown.set(true);
                recognitionListenerAction(ON_ERROR);
                audioShutdown();
                iMic.onError(error);
            }
        }
    }

    /**
     * Force shutdown the microphone and release the resources, without calling any of the
     * listeners
     */
    public void forceAudioShutdown() {

        synchronized (audioLock) {

            if (saiyRecorder != null) {
                isAvailable.set(false);
                Recognition.setState(Recognition.State.IDLE);
                saiyRecorder.shutdown(CLS_NAME);
                saiyRecorder = null;
                fileCreator = null;
            } else {
                if (DEBUG) {
                    MyLog.v(CLS_NAME, "forceAudioShutdown: saiyRecorder already null");
                }
            }

            System.gc();
        }
    }

    /**
     * Shutdown the microphone and release the resources
     */
    private void audioShutdown() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "audioShutdown");
        }

        synchronized (audioLock) {

            if (saiyRecorder != null) {
                isAvailable.set(false);
                ssp.play(ssp.getBeepStop());
                Recognition.setState(Recognition.State.IDLE);
                saiyRecorder.shutdown(CLS_NAME);
                saiyRecorder = null;
                releaseLock();
                recognitionListenerAction(ON_END_OF_SPEECH);
                iMic.onRecordingEnded();
            } else {
                if (DEBUG) {
                    MyLog.v(CLS_NAME, "audioShutdown: saiyRecorder already null");
                }
            }

            if (writeToFile.get()) {
                writeToFile.set(false);
                iMic.onFileWriteComplete(fileCreator.completeWrite());
            }

            if (DEBUG) {
                MyLog.v(CLS_NAME, "audioShutdown: finished synchronisation");
            }

            System.gc();
        }
    }

    /**
     * Notify waiting threads
     */
    private void releaseLock() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "audioShutdown: releaseLock");
        }
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    /**
     * Send callbacks through to our {@link SaiyRecognitionListener}
     *
     * @param action of the listener
     */
    private void recognitionListenerAction(final int action) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "recognitionListenerAction");
        }

        if (listener == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "recognitionListenerAction: listener not required");
            }
            return;
        }

        switch (action) {

            case ON_READY_FOR_SPEECH:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "recognitionListenerAction: ON_READY_FOR_SPEECH");
                }
                listener.onReadyForSpeech(null);
                break;
            case ON_BEGINNING_OF_SPEECH:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "recognitionListenerAction: ON_BEGINNING_OF_SPEECH");
                }
                listener.onBeginningOfSpeech();
                break;
            case ON_ERROR:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "recognitionListenerAction: ON_ERROR");
                }
                listener.onError(SpeechRecognizer.ERROR_AUDIO);
                break;
            case ON_END_OF_SPEECH:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "recognitionListenerAction: ON_END_OF_SPEECH");
                }
                listener.onEndOfSpeech();
                break;
        }
    }
}
