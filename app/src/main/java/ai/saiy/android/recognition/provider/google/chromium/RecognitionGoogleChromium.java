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

package ai.saiy.android.recognition.provider.google.chromium;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Process;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.HttpsURLConnection;

import ai.saiy.android.api.language.vr.VRLanguageGoogle;
import ai.saiy.android.audio.SaiyRecorder;
import ai.saiy.android.audio.SaiySoundPool;
import ai.saiy.android.audio.pause.PauseDetector;
import ai.saiy.android.audio.pause.PauseListener;
import ai.saiy.android.configuration.GoogleConfiguration;
import ai.saiy.android.recognition.Recognition;
import ai.saiy.android.recognition.SaiyRecognitionListener;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

/**
 * Class to use the unofficial Chrome Speech API. You'll need to register in the Chromium Google
 * group and enable the feature in your the Google API console. **BOTH ARE REQUIRED!!**
 * The API key should be entered in the {@link GoogleConfiguration} file.
 * <p>
 * Google's proposition here is just fantastic. If only the Android version of this in Google Now
 * offered such control. It's lightning quick and pretty damn accurate.
 * <p>
 * Created by benrandall76@gmail.com on 12/02/2016.
 * <p>
 * Adapted from posts originating from https://mikepultz.com/2013/07/google-speech-api-full-duplex-php-version
 */
public class RecognitionGoogleChromium implements PauseListener {

    // TODO - The interface is messy as multiple errors of the same type can be thrown from each thread

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = RecognitionGoogleChromium.class.getSimpleName();

    private static final long MIN = 10000000;
    private static final long MAX = 900000009999998L;

    private static final String RESULT = "result";
    private static final String TRANSCRIPT = "transcript";
    private static final String CONFIDENCE = "confidence";
    private static final String ALTERNATIVE = "alternative";
    private static final String FINAL = "final";
    private static final String RESULTS_THREAD = "resultsThread";
    private static final String AUDIO_THREAD = "audioThread";
    private static final String TRANSFER_ENCODING = "Transfer-Encoding";
    private static final String CHUNKED = "chunked";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_AUDIO_PARAMS = "audio/l16; rate=8000";

    private static final int ERROR_HTTP = 1;
    private static final int ERROR_STREAM = 2;
    private static final int ERROR_AUDIO = 3;
    private static final int ERROR_API = 4;
    private static final int ERROR_TIMEOUT = 5;

    private static final String GOOGLE_DUPLEX_SPEECH_BASE = "https://www.google.com/speech-api/full-duplex/v1/";
    private static final String RESULTS_URL = GOOGLE_DUPLEX_SPEECH_BASE + "down?maxresults=1&pair=";
    private static final String AUDIO_URL = GOOGLE_DUPLEX_SPEECH_BASE + "up?lm=dictation&interim&client=chromium&key=";

    private final String LANGUAGE = "&lang=";
    private final String PAIR = "&pair=";

    private final Object audioLock = new Object();
    private final Object errorLock = new Object();

    private static final int nChannels = 1;

    private final int audioSource = MediaRecorder.AudioSource.VOICE_RECOGNITION;
    private final int sampleRateInHz = 8000;
    private final int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

    private volatile SaiyRecorder saiyRecorder;
    private final AtomicBoolean isRecording = new AtomicBoolean();
    private volatile boolean thrown;
    private volatile boolean haveResults;

    private final SaiySoundPool ssp;

    private PauseDetector pauseDetector;

    private final SaiyRecognitionListener listener;
    private final VRLanguageGoogle language;
    private final String apiKey;
    private final boolean pauseDetection;

    /**
     * Constructor
     *
     * @param listener       the associated {@link SaiyRecognitionListener}
     * @param language       the Locale we are using to analyse the voice data. This is not necessarily the
     *                       Locale of the device, as the user may be multi-lingual and have set a custom
     *                       recognition language in a launcher short-cut.
     * @param apiKey         the Chromium Google API key
     * @param pauseDetection if pause detection is required
     */
    public RecognitionGoogleChromium(@NonNull final SaiyRecognitionListener listener,
                                     @NonNull final VRLanguageGoogle language, @NonNull final String apiKey,
                                     final boolean pauseDetection, @NonNull final SaiySoundPool ssp) {
        this.listener = listener;
        this.language = language;
        this.apiKey = apiKey;
        this.pauseDetection = pauseDetection;
        this.ssp = ssp;

        if (this.pauseDetection) {
            pauseDetector = new PauseDetector(this, sampleRateInHz, nChannels,
                    PauseDetector.DEFAULT_PAUSE_IGNORE_TIME);
        }

        saiyRecorder = new SaiyRecorder(audioSource, sampleRateInHz, channelConfig, audioFormat, true);
    }

    @Override
    public void onPauseDetected() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onPauseDetected");
        }

        if (isRecording.get()) {
            stopListening();
        }
    }

    /**
     * Stop the recognition.
     */
    public void stopListening() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "called stopRecording");
        }

        isRecording.set(false);
        Recognition.setState(Recognition.State.PROCESSING);
    }

    /**
     * Start the recognition.
     */
    public void startListening() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "called startRecording");
        }

        isRecording.set(true);
        thrown = false;

        ssp.play(ssp.getBeepStart());

        final long apiPair = MIN + (long) (Math.random() * ((MAX - MIN) + 1L));

        final Thread resultsThread = new Thread() {

            public void run() {
                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);

                try {

                    final URL url = new URL(RESULTS_URL + apiPair);

                    final HttpsURLConnection httpConnResults = (HttpsURLConnection) url.openConnection();
                    httpConnResults.setAllowUserInteraction(false);
                    httpConnResults.setInstanceFollowRedirects(true);
                    httpConnResults.setRequestMethod(Constants.HTTP_GET);
                    httpConnResults.connect();
                    final int responseCode = httpConnResults.getResponseCode();

                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "resultsThread responseCode: " + responseCode);
                    }

                    if (responseCode == HttpsURLConnection.HTTP_OK) {

                        final InputStream inStream = httpConnResults.getInputStream();

                        if (inStream != null) {

                            final Scanner scanner = new Scanner(inStream);

                            while (scanner.hasNextLine()) {
                                parseResults(scanner.nextLine());
                            }

                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "resultsThread stream closed");
                            }

                            closeResources(inStream, scanner, httpConnResults);

                            if (isRecording.get()) {
                                if (DEBUG) {
                                    MyLog.w(CLS_NAME, "isRecording true");
                                }

                                isRecording.set(false);

                                audioShutdown(RESULTS_THREAD);
                                handleError(ERROR_TIMEOUT);
                            } else {
                                if (DEBUG) {
                                    MyLog.w(CLS_NAME, "isRecording false");
                                }

                                if (!haveResults) {
                                    listener.onError(SpeechRecognizer.ERROR_NO_MATCH);
                                }
                            }

                            System.gc();

                        } else {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "resultsThread inStream: null");
                            }

                            audioShutdown(RESULTS_THREAD);
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.e(CLS_NAME, "resultsThread ErrorStream: "
                                    + UtilsString.streamToString(httpConnResults.getErrorStream()));
                        }

                        audioShutdown(RESULTS_THREAD);

                        switch (responseCode) {

                            case 400:
                                handleError(ERROR_STREAM);
                                break;
                            case 403:
                                handleError(ERROR_API);
                                break;
                            default:
                                handleError(ERROR_STREAM);
                                break;

                        }
                    }

                } catch (final MalformedURLException e) {
                    if (DEBUG) {
                        MyLog.e(CLS_NAME, "resultsThread MalformedURLException");
                        e.printStackTrace();
                    }
                    audioShutdown(RESULTS_THREAD);
                    handleError(ERROR_HTTP);
                } catch (final IOException e) {
                    if (DEBUG) {
                        MyLog.e(CLS_NAME, "resultsThread IOException");
                        e.printStackTrace();
                    }
                    audioShutdown(RESULTS_THREAD);
                    handleError(ERROR_HTTP);
                } catch (final NullPointerException e) {
                    if (DEBUG) {
                        MyLog.e(CLS_NAME, "resultsThread NullPointerException");
                        e.printStackTrace();
                    }
                    audioShutdown(RESULTS_THREAD);
                    handleError(ERROR_HTTP);
                } catch (final Exception e) {
                    if (DEBUG) {
                        MyLog.e(CLS_NAME, "resultsThread Exception");
                        e.printStackTrace();
                    }
                    audioShutdown(RESULTS_THREAD);
                    handleError(ERROR_HTTP);
                }
            }
        };

        final Thread audioThread = new Thread() {

            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

                if (pauseDetection) {
                    pauseDetector.begin();
                }

                final int bufferSize = saiyRecorder.getBufferSize();

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "audioThread: bufferSize: " + bufferSize);
                }

                final byte[] buffer = new byte[bufferSize];

                switch (saiyRecorder.initialise()) {

                    case AudioRecord.STATE_INITIALIZED:

                        try {

                            final URL url = new URL(AUDIO_URL + apiKey + LANGUAGE + language + PAIR + apiPair);
                            final URLConnection urlConn = url.openConnection();

                            final HttpsURLConnection httpConnAudio = (HttpsURLConnection) urlConn;
                            httpConnAudio.setAllowUserInteraction(false);
                            httpConnAudio.setInstanceFollowRedirects(true);
                            httpConnAudio.setRequestMethod(Constants.HTTP_POST);
                            httpConnAudio.setDoOutput(true);
                            httpConnAudio.setRequestProperty(TRANSFER_ENCODING, CHUNKED);
                            httpConnAudio.setChunkedStreamingMode(0);
                            httpConnAudio.setRequestProperty(CONTENT_TYPE, CONTENT_TYPE_AUDIO_PARAMS);

                            httpConnAudio.connect();

                            final OutputStream out = httpConnAudio.getOutputStream();

                            switch (saiyRecorder.startRecording()) {

                                case AudioRecord.RECORDSTATE_RECORDING: {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "audioThread: Check: AudioRecord.RECORDSTATE_RECORDING");
                                    }

                                    int count = 0;
                                    while (isRecording.get() && saiyRecorder != null
                                            && saiyRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {

                                        if (count == 0) {
                                            if (DEBUG) {
                                                MyLog.i(CLS_NAME, "Recording Started");
                                            }

                                            Recognition.setState(Recognition.State.LISTENING);

                                            listener.onReadyForSpeech(null);
                                            resultsThread.start();
                                            count++;
                                        }

                                        if (saiyRecorder != null) {
                                            final int bufferReadResult = saiyRecorder.read(buffer);
                                            listener.onBufferReceived(buffer);

                                            if (pauseDetection && !pauseDetector.hasDetected()) {
                                                pauseDetector.addLength(buffer, bufferReadResult);
                                                pauseDetector.monitor();
                                            }

                                            for (int i = 0; i < bufferReadResult; i++) {
                                                out.write(buffer[i]);
                                            }
                                        }
                                    }

                                    audioShutdown(RESULTS_THREAD);

                                    if (out != null) {
                                        try {
                                            out.close();
                                        } catch (final IOException e) {
                                            if (DEBUG) {
                                                MyLog.e(CLS_NAME, "audioThread out.close()");
                                            }
                                        }
                                    }

                                    final int responseCode = httpConnAudio.getResponseCode();

                                    if (DEBUG) {
                                        MyLog.d(CLS_NAME, "audioThread responseCode: " + responseCode);
                                    }

                                    if (responseCode != HttpsURLConnection.HTTP_OK) {
                                        if (DEBUG) {
                                            MyLog.e(CLS_NAME, "audioThread ErrorStream: "
                                                    + UtilsString.streamToString(httpConnAudio.getErrorStream()));
                                        }

                                        audioShutdown(RESULTS_THREAD);
                                        handleError(ERROR_STREAM);

                                    }

                                    try {
                                        httpConnAudio.disconnect();
                                    } catch (final NullPointerException e) {
                                        if (DEBUG) {
                                            MyLog.e(CLS_NAME, "audioThread NullPointerException disconnect()");
                                            e.printStackTrace();
                                        }
                                    } catch (final Exception e) {
                                        if (DEBUG) {
                                            MyLog.e(CLS_NAME, "audioThread Exception disconnect()");
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                break;
                                case AudioRecord.ERROR:
                                    if (DEBUG) {
                                        MyLog.w(CLS_NAME, "audioThread: != AudioRecord.RECORDSTATE_RECORDING");
                                    }
                                    handleError(ERROR_AUDIO);
                                    break;
                            }

                        } catch (final MalformedURLException e) {
                            if (DEBUG) {
                                MyLog.e(CLS_NAME, "audioThread MalformedURLException");
                                e.printStackTrace();
                            }
                            audioShutdown(RESULTS_THREAD);
                            handleError(ERROR_HTTP);
                        } catch (final IOException e) {
                            if (DEBUG) {
                                MyLog.e(CLS_NAME, "audioThread IOException");
                                e.printStackTrace();
                            }
                            audioShutdown(RESULTS_THREAD);
                            handleError(ERROR_HTTP);
                        } catch (final IllegalStateException e) {
                            if (DEBUG) {
                                MyLog.e(CLS_NAME, "audioThread IllegalStateException");
                                e.printStackTrace();
                            }
                            audioShutdown(RESULTS_THREAD);
                            handleError(ERROR_AUDIO);
                        } catch (final NullPointerException e) {
                            if (DEBUG) {
                                MyLog.e(CLS_NAME, "audioThread NullPointerException");
                                e.printStackTrace();
                            }
                            audioShutdown(RESULTS_THREAD);
                            handleError(ERROR_HTTP);
                        } catch (final Exception e) {
                            if (DEBUG) {
                                MyLog.e(CLS_NAME, "audioThread Exception");
                                e.printStackTrace();
                            }
                            audioShutdown(RESULTS_THREAD);
                            handleError(ERROR_HTTP);
                        }

                        audioShutdown(AUDIO_THREAD);

                        break;

                    case AudioRecord.STATE_UNINITIALIZED:
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "AudioRecord.STATE_UNINITIALIZED");
                        }

                        handleError(ERROR_AUDIO);
                        break;
                }
            }
        };

        audioThread.start();
    }

    /**
     * Parse the recognition results
     */
    private void parseResults(final String response) {

        if (!TextUtils.isEmpty(response)) {

            try {

                final StringBuilder stringBuilder = new StringBuilder();

                final JSONObject object = (JSONObject) new JSONTokener(response).nextValue();
                final JSONArray resultArray = object.getJSONArray(RESULT);

                final ArrayList<String> partialArray = new ArrayList<>();
                final ArrayList<String> resultsArray = new ArrayList<>();
                final Bundle resultsBundle = new Bundle();
                final Bundle partialBundle = new Bundle();

                boolean haveFinal;

                for (int i = 0; i < resultArray.length(); i++) {

                    final JSONObject objectHeader = resultArray.getJSONObject(i);

                    final JSONArray alternativeArray = objectHeader.getJSONArray(ALTERNATIVE);

                    haveFinal = objectHeader.has(FINAL);

                    if (haveFinal) {

                        final float[] floatsArray = new float[alternativeArray.length()];

                        for (int j = 0; j < alternativeArray.length(); j++) {
                            final JSONObject transcriptObject = alternativeArray.getJSONObject(j);
                            resultsArray.add(transcriptObject.getString(TRANSCRIPT));

                            if (transcriptObject.has(CONFIDENCE)) {
                                floatsArray[j] = (float) transcriptObject.getDouble(CONFIDENCE);
                            } else {
                                floatsArray[j] = 0.5f;
                            }
                        }

                        if (i == (resultArray.length() - 1)) {
                            resultsBundle.putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, resultsArray);
                            resultsBundle.putFloatArray(SpeechRecognizer.CONFIDENCE_SCORES, floatsArray);
                            listener.onResults(resultsBundle);
                            haveResults = true;
                            break;
                        }

                    } else {

                        for (int j = 0; j < alternativeArray.length(); j++) {
                            final JSONObject transcriptObject = alternativeArray.getJSONObject(j);
                            stringBuilder.append(transcriptObject.getString(TRANSCRIPT));
                        }

                        if (i == (resultArray.length() - 1)) {
                            partialArray.add(stringBuilder.toString());
                            partialBundle.putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, partialArray);
                            listener.onPartialResults(partialBundle);
                            haveResults = true;
                        }
                    }
                }

            } catch (final NullPointerException e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "parseResults JSON NullPointerException");
                    e.printStackTrace();
                }
            } catch (final JSONException e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "parseResults JSONException");
                    e.printStackTrace();
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "parseResults JSON Exception");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Handle errors - TODO - fugly
     */
    private void handleError(final int errorCode) {
        if (DEBUG) {
            MyLog.v(CLS_NAME, "in handleError");
        }

        synchronized (errorLock) {

            if (!thrown) {

                Recognition.setState(Recognition.State.IDLE);
                thrown = true;

                switch (errorCode) {

                    case ERROR_HTTP:
                        if (DEBUG) {
                            MyLog.v(CLS_NAME, "handleError: throwing: ERROR_HTTP");
                        }
                        listener.onError(errorCode);
                        break;
                    case ERROR_STREAM:
                        if (DEBUG) {
                            MyLog.v(CLS_NAME, "handleError: throwing: ERROR_STREAM");
                        }
                        listener.onError(errorCode);
                        break;
                    case ERROR_AUDIO:
                        if (DEBUG) {
                            MyLog.v(CLS_NAME, "handleError: throwing: ERROR_AUDIO");
                        }
                        listener.onError(errorCode);
                        break;
                    case ERROR_API:
                        if (DEBUG) {
                            MyLog.v(CLS_NAME, "handleError: throwing: ERROR_API");
                        }
                        listener.onError(errorCode);
                        break;
                    case ERROR_TIMEOUT:
                        if (DEBUG) {
                            MyLog.v(CLS_NAME, "handleError: throwing: ERROR_TIMEOUT");
                        }
                        listener.onError(errorCode);
                        break;
                    default:
                        if (DEBUG) {
                            MyLog.v(CLS_NAME, "handleError: default throwing: ERROR_HTTP");
                        }
                        listener.onError(ERROR_HTTP);
                        break;
                }

            } else {
                if (DEBUG) {
                    MyLog.v(CLS_NAME, "handleError: thrown already: " + thrown);
                }
            }
        }
    }

    /**
     * Shutdown the microphone and release the resources
     */
    private void audioShutdown(final String from) {
        if (DEBUG) {
            MyLog.v(CLS_NAME, "in audioShutdown: " + from);
        }

        synchronized (audioLock) {

            if (saiyRecorder != null) {
                ssp.play(ssp.getBeepStop());
                Recognition.setState(Recognition.State.IDLE);
                listener.onEndOfSpeech();
                saiyRecorder.shutdown(from);

                if (DEBUG) {
                    MyLog.d(CLS_NAME, "audioShutdown: audioSession set to NULL ~ " + from);
                }

                saiyRecorder = null;

            } else {
                if (DEBUG) {
                    MyLog.v(CLS_NAME, "audioShutdown: NULL ~ " + from);
                }
            }

            if (DEBUG) {
                MyLog.v(CLS_NAME, "audioShutdown: finished synchronisation ~ " + from);
            }
        }
    }

    /**
     * Attempt to shutdown any resources. Only here to avoid clutter above.
     *
     * @param inStream        the InputStream
     * @param scanner         the Scanner
     * @param httpConnResults the HttpsURLConnection
     */
    private void closeResources(final InputStream inStream, final Scanner scanner,
                                final HttpsURLConnection httpConnResults) {

        if (inStream != null) {
            try {
                inStream.close();
            } catch (final IOException e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "resultsThread inStream.close()");
                }
            }
        }

        if (scanner != null) {
            try {
                scanner.close();
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "resultsThread inStream.close()");
                }
            }
        }

        if (httpConnResults != null) {
            try {
                httpConnResults.disconnect();
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "resultsThread httpConnResults.disconnect()");
                }
            }
        }
    }
}

