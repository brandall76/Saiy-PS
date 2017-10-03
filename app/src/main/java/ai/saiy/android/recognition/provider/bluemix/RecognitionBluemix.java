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

package ai.saiy.android.recognition.provider.bluemix;

import android.os.Bundle;
import android.os.Process;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import org.apache.commons.lang3.ArrayUtils;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.util.Base64;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import ai.saiy.android.api.SaiyDefaults;
import ai.saiy.android.api.language.vr.VRLanguageIBM;
import ai.saiy.android.api.remote.Request;
import ai.saiy.android.audio.IMic;
import ai.saiy.android.audio.RecognitionMic;
import ai.saiy.android.cognitive.identity.provider.microsoft.Speaker;
import ai.saiy.android.configuration.BluemixConfiguration;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.nlu.bluemix.Alternative;
import ai.saiy.android.nlu.bluemix.NLUBluemix;
import ai.saiy.android.nlu.bluemix.ResolveBluemix;
import ai.saiy.android.nlu.bluemix.Result;
import ai.saiy.android.recognition.Recognition;
import ai.saiy.android.recognition.SaiyRecognitionListener;
import ai.saiy.android.recognition.provider.bluemix.mod.TrustAllBluemixWebSocketClient;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsLocale;
import ai.saiy.android.utils.UtilsString;

/**
 * Class uses the IBM Bluemix speech SDK. At the time of writing, I find it hard to believe the accuracy
 * could be so poor, so I'm going to assume I'm doing something wrong....
 * <p/>
 * Register at <a href="http://www.ibm.com/cloud-computing/bluemix/">IBM Bluemix</a>
 * and register your credentials in {@link BluemixConfiguration}
 * <p/>
 * Created by benrandall76@gmail.com on 21/09/2016.
 */
public class RecognitionBluemix implements IMic, IWebSocketCallback {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = RecognitionBluemix.class.getSimpleName();

    private static final String AUTHORIZATION = "Authorization";
    private static final String CONTENT_TYPE = "content-type";
    private static final long PAUSE_TIMEOUT = 750L;
    private static final long FINAL_WAIT = 2500L;
    private static final String AUDIO_FORMAT_DEFAULT = "audio/l16;rate=16000";

    private static final String ACTION = "action";
    private static final String BASIC = "Basic ";
    private static final String START = "start";
    private static final String DELIMITER = ":";
    private static final String INTERIM_RESULTS = "interim_results";
    private static final String CONTINUOUS = "continuous";
    private static final String MAX_ALTERNATIVES = "max_alternatives";
    private static final int MAX_ALTERNATIVE_VALUE = 5;
    private static final String PROFANITY_FILTER = "profanity_filter";
    private static final String INACTIVITY_TIMEOUT = "inactivity_timeout";

    private final AtomicBoolean isRecording = new AtomicBoolean();
    private final AtomicBoolean doBeginning = new AtomicBoolean(true);
    private final AtomicBoolean doError = new AtomicBoolean(true);
    private final AtomicBoolean doRecordingEnded = new AtomicBoolean(true);
    private final AtomicBoolean haveFinal = new AtomicBoolean(false);
    private final AtomicBoolean doneHandshake = new AtomicBoolean();
    private final AtomicInteger count = new AtomicInteger();

    private final ArrayList<String> partialArray = new ArrayList<>();
    private final ArrayList<String> resultsArray = new ArrayList<>();
    private final ArrayList<Float> confidenceArray = new ArrayList<>();
    private final Bundle bundle = new Bundle();

    private volatile TrustAllBluemixWebSocketClient client;

    private final RecognitionMic mic;
    private final SaiyRecognitionListener listener;
    private final SaiyDefaults.LanguageModel languageModel;
    private final Locale ttsLocale;
    private final VRLanguageIBM vrLocale;
    private final SupportedLanguage sl;
    private final boolean servingRemote;

    /**
     * Constructor
     *
     * @param listener      the associated {@link SaiyRecognitionListener}
     * @param userName      the Bluemix service user name
     * @param password      the Bluemix service password
     * @param languageModel the {@link SaiyDefaults.LanguageModel}
     * @param ttsLocale     the Text to Speech {@link Locale}
     * @param vrLocale      the {@link VRLanguageIBM}
     * @param sl            the {@link SupportedLanguage}
     * @param servingRemote true if the origin is a remote request
     */
    public RecognitionBluemix(@NonNull final SaiyRecognitionListener listener,
                              @NonNull final String userName,
                              @NonNull final String password,
                              @NonNull final SaiyDefaults.LanguageModel languageModel,
                              @NonNull final Locale ttsLocale, @NonNull final VRLanguageIBM vrLocale,
                              @NonNull final SupportedLanguage sl,
                              final boolean servingRemote, @NonNull final RecognitionMic mic) {
        this.listener = listener;
        this.languageModel = languageModel;
        this.ttsLocale = ttsLocale;
        this.vrLocale = vrLocale;
        this.sl = sl;
        this.servingRemote = servingRemote;
        this.mic = mic;

        this.mic.setMicListener(this);

        if (DEBUG) {
            MyLog.i(CLS_NAME, "language: " + vrLocale.getLocaleString());
            MyLog.i(CLS_NAME, "model: " + vrLocale.getModel());
        }

        final String auth = BASIC + Base64.encodeBytes((userName + DELIMITER + password)
                .getBytes(Charset.forName(Constants.ENCODING_UTF8)));

        final HashMap<String, String> header = new HashMap<>();
        header.put(CONTENT_TYPE, AUDIO_FORMAT_DEFAULT);
        header.put(AUTHORIZATION, auth);

        //noinspection ConstantConditions
        client = new TrustAllBluemixWebSocketClient(BluemixConfiguration.getSpeechURI(this.vrLocale.getModel()),
                header, this);
    }

    /**
     * Attempt to start the client, catching any exceptions
     *
     * @return true if the client started successfully, false otherwise
     */
    private boolean startClient() {

        try {
            client.start();
            return true;
        } catch (final NoSuchAlgorithmException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "NoSuchAlgorithmException");
                e.printStackTrace();
            }
        } catch (final KeyManagementException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "KeyManagementException");
                e.printStackTrace();
            }
        } catch (final InterruptedException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "InterruptedException");
                e.printStackTrace();
            }
        } catch (final CertificateException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "CertificateException");
                e.printStackTrace();
            }
        }

        return false;
    }


    /**
     * Start the recognition.
     */
    public void startListening() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "startListening");
        }

        if (doError.get()) {
            if (mic.isAvailable()) {
                if (client != null) {
                    isRecording.set(true);
                    mic.startRecording();

                    if (startClient()) {

                        new Thread() {
                            public void run() {
                                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);

                                try {

                                    synchronized (mic.getLock()) {
                                        while (mic.isRecording()) {
                                            try {
                                                if (DEBUG) {
                                                    MyLog.i(CLS_NAME, "mic lock waiting");
                                                }
                                                mic.getLock().wait();
                                            } catch (final InterruptedException e) {
                                                if (DEBUG) {
                                                    MyLog.e(CLS_NAME, "InterruptedException");
                                                    e.printStackTrace();
                                                }
                                                break;
                                            }
                                        }
                                    }

                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "record lock released: interrupted: " + mic.isInterrupted());
                                    }

                                } catch (final IllegalStateException e) {
                                    if (DEBUG) {
                                        MyLog.e(CLS_NAME, "IllegalStateException");
                                        e.printStackTrace();
                                    }
                                    onError(SpeechRecognizer.ERROR_NETWORK);
                                } catch (final NullPointerException e) {
                                    if (DEBUG) {
                                        MyLog.w(CLS_NAME, "NullPointerException");
                                        e.printStackTrace();
                                    }
                                    onError(SpeechRecognizer.ERROR_NETWORK);
                                } catch (final Exception e) {
                                    if (DEBUG) {
                                        MyLog.w(CLS_NAME, "Exception");
                                        e.printStackTrace();
                                    }
                                    onError(SpeechRecognizer.ERROR_NETWORK);
                                } finally {
                                    stopListening();
                                }
                            }
                        }.start();
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "startListening client error");
                        }

                        onError(SpeechRecognizer.ERROR_NETWORK);
                        mic.forceAudioShutdown();
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "startListening client null");
                    }

                    onError(SpeechRecognizer.ERROR_NETWORK);
                    mic.forceAudioShutdown();
                }
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "startListening mic unavailable");
                }

                onError(Speaker.ERROR_AUDIO);
                mic.forceAudioShutdown();
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "startListening error already thrown");
            }

            mic.forceAudioShutdown();
        }
    }

    /**
     * Stop the recognition.
     */
    public void stopListening() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "called stopRecording");
        }

        if (isRecording.get()) {
            isRecording.set(false);

            new Thread() {
                public void run() {
                    android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);
                    mic.stopRecording();
                    Recognition.setState(Recognition.State.IDLE);

                    if (!haveFinal.get() || !doError.get()) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "stopRecording: sleeping");
                        }

                        try {
                            Thread.sleep(FINAL_WAIT);
                        } catch (final InterruptedException e) {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "stopRecording: InterruptedException");
                                e.printStackTrace();
                            }
                        }
                    }

                    closeConnection();
                }
            }.start();
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "called stopRecording: isRecording false");
            }
        }
    }

    /**
     * Close the web socket connection
     */
    private void closeConnection() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "closeConnection");
        }

        isRecording.set(false);

        if (client != null) {

            try {
                client.disconnect();
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "closeConnection: Exception");
                    e.printStackTrace();
                }
            } finally {
                client = null;
            }
        }
    }

    @Override
    public void onBufferReceived(final int bufferReadResult, final byte[] buffer) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onBufferReceived");
        }

        if (client != null && isRecording.get()) {

            count.set(count.get() + 1);

            if (count.get() == 1 && doBeginning.get()) {
                doBeginning.set(false);
                listener.onBeginningOfSpeech();
            }

            final WebSocket.READYSTATE readyState = client.getReadyState();

            // TODO - missed audio
            switch (readyState) {

                case NOT_YET_CONNECTED:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "READYSTATE: NOT_YET_CONNECTED");
                    }
                    break;
                case CONNECTING:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "READYSTATE: CONNECTING");
                    }
                    break;
                case OPEN:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "READYSTATE: OPEN");
                    }

                    if (doneHandshake.get()) {
                        client.send(buffer);
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "Waiting for handshake");
                        }

                        if (count.get() > 10) {
                            onError(new Exception("AudioRecord.ERROR"));
                        }
                    }

                    break;
                case CLOSING:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "READYSTATE: CLOSING");
                    }
                    break;
                case CLOSED:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "READYSTATE: CLOSED");
                    }
                    break;
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onBufferReceived: recording finished");
            }
        }
    }

    @Override
    public void onError(final int error) {
        if (DEBUG) {
            MyLog.w(CLS_NAME, "onError");
        }

        if (doError.get()) {
            doError.set(false);
            stopListening();

            Recognition.setState(Recognition.State.IDLE);

            if (mic.isInterrupted()) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onError: isInterrupted");
                }

                listener.onError(error);
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onError: listener onComplete");
                }

                listener.onComplete();
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onError: doError false");
            }
        }
    }

    @Override
    public void onPauseDetected() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onPauseDetected");
        }

        if (isRecording.get()) {
            stopListening();
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onPauseDetected: isRecording false");
            }
        }
    }

    @Override
    public void onRecordingStarted() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onRecordingStarted");
        }
        listener.onReadyForSpeech(null);
    }

    @Override
    public void onRecordingEnded() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onRecordingEnded");
        }

        if (doRecordingEnded.get()) {
            doRecordingEnded.set(false);
            listener.onEndOfSpeech();
            listener.onComplete();
            Recognition.setState(Recognition.State.IDLE);
        }
    }

    @Override
    public void onFileWriteComplete(final boolean success) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onFileWriteComplete: " + success);
        }
    }


    @Override
    public void onOpen(final ServerHandshake handshakeData) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onOpen");
        }

        final JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put(ACTION, START);
            jsonObject.put(CONTENT_TYPE, AUDIO_FORMAT_DEFAULT);
            jsonObject.put(INTERIM_RESULTS, true);
            jsonObject.put(CONTINUOUS, true);
            jsonObject.put(MAX_ALTERNATIVES, MAX_ALTERNATIVE_VALUE);
            jsonObject.put(PROFANITY_FILTER, false);
            jsonObject.put(INACTIVITY_TIMEOUT, PAUSE_TIMEOUT);
        } catch (final JSONException e) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onOpen: JSONException");
                e.printStackTrace();
            }
        }

        client.send(jsonObject.toString());
        doneHandshake.set(true);
    }

    @Override
    public void onMessage(final String message) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onMessage: " + message);
        }

        final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        NLUBluemix nluBluemix = null;

        try {
            nluBluemix = gson.fromJson(message, NLUBluemix.class);
        } catch (final JsonSyntaxException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "JsonSyntaxException");
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

        if (nluBluemix != null) {

            if (UtilsString.notNaked(nluBluemix.getState())) {
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "Status message: " + nluBluemix.getState());
                }
            } else {

                partialArray.clear();
                resultsArray.clear();
                confidenceArray.clear();
                bundle.clear();

                final List<Result> results = nluBluemix.getResults();

                if (UtilsList.notNaked(results)) {

                    final int resultsSize = results.size();
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "results size: " + resultsSize);
                    }

                    if (!detectFinal(results)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onMessage: have partial");
                        }

                        for (int i = 0; i < resultsSize; i++) {

                            final List<Alternative> alternatives = results.get(i).getAlternatives();

                            final int size = alternatives.size();
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "partial alternatives size: " + size);
                            }

                            for (int j = 0; j < size; j++) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "final result: " + alternatives.get(j).getTranscript()
                                            + " ~ " + alternatives.get(j).getConfidence());
                                }

                                partialArray.add(alternatives.get(j).getTranscript().trim());
                            }
                        }

                        bundle.putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, partialArray);
                        listener.onPartialResults(bundle);

                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onMessage: have final");
                        }

                        haveFinal.set(true);

                        if (doError.get()) {
                            stopListening();
                        }

                        for (int i = 0; i < resultsSize; i++) {

                            final List<Alternative> alternatives = results.get(i).getAlternatives();

                            final int size = alternatives.size();
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "final alternatives size: " + size);
                            }

                            for (int j = 0; j < size; j++) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "final result: " + alternatives.get(j).getTranscript()
                                            + " ~ " + alternatives.get(j).getConfidence());
                                }

                                confidenceArray.add(alternatives.get(j).getConfidence());
                                resultsArray.add(alternatives.get(j).getTranscript().trim());
                            }
                        }

                        Recognition.setState(Recognition.State.IDLE);

                        if (languageModel == SaiyDefaults.LanguageModel.IBM) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "final: nlu required");
                            }

                            if (servingRemote) {
                                bundle.putString(Request.RESULTS_NLU, message);
                                bundle.putFloatArray(SpeechRecognizer.CONFIDENCE_SCORES,
                                        ArrayUtils.toPrimitive(confidenceArray.toArray(new Float[0]), 0.0F));
                                bundle.putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, resultsArray);
                                listener.onResults(bundle);
                            } else {
                                new ResolveBluemix(mic.getContext(), sl, UtilsLocale.stringToLocale(vrLocale.toString()),
                                        ttsLocale, ArrayUtils.toPrimitive(confidenceArray.toArray(new Float[0]), 0.0F),
                                        resultsArray).unpack(nluBluemix);
                            }
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "final: nlu not required");
                            }

                            bundle.putFloatArray(SpeechRecognizer.CONFIDENCE_SCORES,
                                    ArrayUtils.toPrimitive(confidenceArray.toArray(new Float[0]), 0.0F));
                            bundle.putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, resultsArray);
                            listener.onResults(bundle);
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onMessage: nluBluemix results naked");
                    }
                }
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "onMessage: nluBluemix null");
            }
        }
    }

    /**
     * Loop through the {@link Result} list to detect a final parameter
     *
     * @param results the list of {@link Result}
     * @return true if the final parameter is detected, false otherwise
     */
    private boolean detectFinal(@NonNull final List<Result> results) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "detectFinal");
        }

        for (final Result result : results) {
            if (result.isFinal()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onClose(final int code, final String reason, final boolean remote) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onClose: " + reason + " ~ " + remote);
        }

        // TODO
        if (remote && (!doError.get() || isRecording.get())) {
            onError(SpeechRecognizer.ERROR_NETWORK);
        }
    }

    @Override
    public void onError(final Exception e) {
        if (DEBUG) {
            MyLog.w(CLS_NAME, "onError");
            e.printStackTrace();
        }
        onError(SpeechRecognizer.ERROR_NETWORK);
    }
}
