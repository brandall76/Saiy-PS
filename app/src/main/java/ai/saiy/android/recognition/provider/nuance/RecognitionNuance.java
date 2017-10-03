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

package ai.saiy.android.recognition.provider.nuance;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import com.nuance.speechkit.Audio;
import com.nuance.speechkit.DetectionType;
import com.nuance.speechkit.Interpretation;
import com.nuance.speechkit.Language;
import com.nuance.speechkit.RecognitionType;
import com.nuance.speechkit.RecognizedPhrase;
import com.nuance.speechkit.Session;
import com.nuance.speechkit.Transaction;
import com.nuance.speechkit.TransactionException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.api.SaiyDefaults;
import ai.saiy.android.api.language.vr.VRLanguageNuance;
import ai.saiy.android.api.remote.Request;
import ai.saiy.android.command.cancel.Cancel;
import ai.saiy.android.configuration.NuanceConfiguration;
import ai.saiy.android.localisation.SaiyResources;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.nlu.nuance.ResolveNuance;
import ai.saiy.android.recognition.Recognition;
import ai.saiy.android.recognition.SaiyRecognitionListener;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsLocale;

/**
 * This class uses the SpeechKit SDK from Nuance to analyse the voice recognition.
 * <p/>
 * Unfortunately, it's a pretty poor effort and is slow and lacking in essential features such as
 * partial results and a way to control the pause-timeout, which just doesn't work properly. The
 * result is to have to use workarounds to detect commands we would have analysed using the
 * partial results.
 * <p/>
 * You'll need to register <a href="https://developer.nuance.com">Nuance Developers</a> to get an
 * API key for either just the recognition or for the NLU platform too. The details need to be
 * entered in the {@link NuanceConfiguration}file.
 * <p/>
 * Created by benrandall76@gmail.com on 07/02/2016.
 */
public class RecognitionNuance {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = RecognitionNuance.class.getSimpleName();

    private static final String RECORDER = "recorder";
    private static final String RETRY = "retry";
    private static final String CANCELLED = "cancelled";

    private final Session speechSession;
    private Transaction recoTransaction;
    private final Transaction.Options options;

    private final SaiyRecognitionListener listener;

    private ArrayList<String> resultsArray;
    private float[] floatsArray;
    private boolean isCancelled;

    private final SaiyDefaults.LanguageModel languageModel;
    private final Context mContext;
    private final boolean servingRemote;
    private final String contextTag;
    private final Locale ttsLocale;
    private final VRLanguageNuance vrLocale;
    private final SupportedLanguage sl;

    /**
     * Constructor
     *
     * @param mContext      the application context
     * @param listener      the {@link SaiyRecognitionListener}
     * @param detectionType one of {@link RecognitionType#DICTATION} {@link RecognitionType#SEARCH}
     * @param serverUri     the URI of the Nuance Recognition
     * @param nluServerUri  the URI of the Nuance Mix Recognition
     * @param appKey        for Nuance
     * @param contextTag    for the Mix.nlu language model
     * @param servingRemote whether this is a local request
     * @param languageModel how to resolve the utterance
     * @param ttsLocale     the Text to Speech {@link Locale}
     * @param vrLocale      the {@link VRLanguageNuance}
     * @param sl            the {@link SupportedLanguage}
     */
    @MainThread
    public RecognitionNuance(@NonNull final Context mContext, @NonNull final SaiyRecognitionListener listener,
                             @NonNull final DetectionType detectionType, @NonNull final Uri serverUri,
                             @NonNull final Uri nluServerUri, @NonNull final String appKey, @NonNull final String contextTag,
                             final boolean servingRemote, @NonNull final SaiyDefaults.LanguageModel languageModel,
                             @NonNull final Locale ttsLocale, @NonNull final VRLanguageNuance vrLocale,
                             @NonNull final SupportedLanguage sl) {
        this.mContext = mContext.getApplicationContext();
        this.listener = listener;
        this.servingRemote = servingRemote;
        this.contextTag = contextTag;
        this.languageModel = languageModel;
        this.ttsLocale = ttsLocale;
        this.vrLocale = vrLocale;
        this.sl = sl;

        if (languageModel != SaiyDefaults.LanguageModel.NUANCE) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "serverUri: " + nluServerUri);
                MyLog.i(CLS_NAME, "appKey: " + appKey);
            }
            speechSession = Session.Factory.session(this.mContext, serverUri, appKey);
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "nluServerUri: " + nluServerUri);
                MyLog.i(CLS_NAME, "appKey: " + appKey);
            }
            speechSession = Session.Factory.session(this.mContext, nluServerUri, appKey);
        }

        final Audio startEarcon = new Audio(this.mContext, ai.saiy.android.R.raw.sk_start, NuanceConfiguration.PCM_FORMAT);
        final Audio stopEarcon = new Audio(this.mContext, ai.saiy.android.R.raw.sk_stop, NuanceConfiguration.PCM_FORMAT);
        final Audio errorEarcon = new Audio(this.mContext, ai.saiy.android.R.raw.sk_error, NuanceConfiguration.PCM_FORMAT);

        options = new Transaction.Options();
        options.setDetection(detectionType);
        options.setEarcons(startEarcon, stopEarcon, errorEarcon, null);

        if (languageModel != SaiyDefaults.LanguageModel.NUANCE) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "nlu not required");
            }

            options.setLanguage(new Language(this.vrLocale.getLocaleString()));
            options.setRecognitionType(RecognitionType.DICTATION);
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "nlu required");
            }

            if (DEBUG) {
                MyLog.i(CLS_NAME, "language: " + vrLocale.getLocaleString());
            }

            // Currently eng-USA only supported
            if (!vrLocale.equals(VRLanguageNuance.ENGLISH_US)) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "unsupported Mix.NLU language: " + vrLocale.getLocaleString());
                }

                options.setLanguage(new Language(VRLanguageNuance.ENGLISH_US.getLocaleString()));
            } else {
                options.setLanguage(new Language(vrLocale.getLocaleString()));
            }
        }

    }

    /**
     * Start the recognition
     */
    public void startListening() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "startListening");
        }


        if (languageModel != SaiyDefaults.LanguageModel.NUANCE) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "nlu not required");
            }
            recoTransaction = speechSession.recognize(options, recoListener);
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "LanguageModelDefault.NUANCE");
                MyLog.i(CLS_NAME, "contextTag: " + contextTag);
            }
            recoTransaction = speechSession.recognizeWithService(contextTag,
                    new JSONObject(), options, recoListener);
        }
    }


    /**
     * Stop the recognition
     */
    public void stopListening() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "stopListening");
        }

        recoTransaction.stopRecording();
    }

    /**
     * Stop the recognition and cancel any pending requests
     */
    public void cancelListening() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "cancelListening");
        }

        // Check that this hasn't been called by us
        if (!isCancelled) {
            recoTransaction.cancel();
        }

        // @onFinishedRecording not always called
        Recognition.setState(Recognition.State.IDLE);
    }

    /**
     * Listener for recognition events
     */
    private final Transaction.Listener recoListener = new Transaction.Listener() {

        @Override
        public void onStartedRecording(final Transaction transaction) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onStartedRecording");
            }

            Recognition.setState(Recognition.State.LISTENING);

            listener.onReadyForSpeech(null);
            listener.onBeginningOfSpeech();
        }

        @Override
        public void onFinishedRecording(final Transaction transaction) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onFinishedRecording");
            }

            // check for race condition against onError
            switch (Recognition.getState()) {
                case LISTENING:
                    Recognition.setState(Recognition.State.PROCESSING);
                    break;
            }

            // check for race condition against onError
            if (Recognition.getState() != Recognition.State.IDLE) {
                listener.onEndOfSpeech();
            }
        }

        @Override
        public void onRecognition(final Transaction transaction, final com.nuance.speechkit.Recognition recognition) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onRecognition");
            }

            int size = recognition.getDetails().size();
            floatsArray = new float[size];
            resultsArray = new ArrayList<>(size);

            for (int i = 0; i < size; i++) {

                final RecognizedPhrase phrase = recognition.getDetails().get(i);

                resultsArray.add(phrase.getText());
                floatsArray[i] = (float) phrase.getConfidence();
            }

            final Bundle results = new Bundle();
            results.putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, resultsArray);
            results.putFloatArray(SpeechRecognizer.CONFIDENCE_SCORES, floatsArray);

            if (languageModel != SaiyDefaults.LanguageModel.NUANCE) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "nlu not required: triggering onResults");
                }
                listener.onResults(results);
            } else {
                // Due to Nuance not supporting partial results, we need to check for a cancel
                // command so we can react quickly and not waste resource. This is a race condition.
                isCancelled = !servingRemote && new Cancel(sl, new SaiyResources(mContext, sl),
                        true).detectPartial(results);

                if (isCancelled) {
                    listener.onResults(results);
                }
            }

            Recognition.setState(Recognition.State.IDLE);
        }

        @Override
        public void onInterpretation(final Transaction transaction, final Interpretation interpretation) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onInterpretation");

                try {
                    MyLog.d(CLS_NAME, interpretation.getResult().toString(2));
                } catch (final JSONException e) {
                    e.printStackTrace();
                }
            }

            Recognition.setState(Recognition.State.IDLE);

            // We would expect the recognition results to be returned first
            // Check if the user has cancelled the request.
            if (!isCancelled) {
                if (servingRemote) {
                    final Bundle results = new Bundle();
                    results.putStringArrayList(Request.RESULTS_RECOGNITION, resultsArray);
                    results.putFloatArray(Request.CONFIDENCE_SCORES, floatsArray);
                    results.putString(Request.RESULTS_NLU, interpretation.getResult().toString());
                    listener.onResults(results);
                } else {
                    new ResolveNuance(mContext, sl, UtilsLocale.stringToLocale(vrLocale.getLocaleString()),
                            ttsLocale, floatsArray, resultsArray).unpack(interpretation.getResult());
                }
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onInterpretation: cancelled ignoring");
                }
            }
        }

        @Override
        public void onSuccess(final Transaction transaction, final String s) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onSuccess");
            }

            tidyUp();
        }

        @Override
        public void onError(final Transaction transaction, final String s,
                            final TransactionException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "onError: " + e.getMessage() + " ~ " + s);
            }

            // this could be null
            if (e != null && e.getMessage() != null) {

                // not the greatest way to differentiate between errors
                final String errorMessage = e.getMessage();

                if (errorMessage.contains(RETRY)) {
                    listener.onError(SpeechRecognizer.ERROR_NO_MATCH);
                } else if (errorMessage.contains(RECORDER)) {
                    listener.onError(SpeechRecognizer.ERROR_AUDIO);
                } else if (errorMessage.contains(CANCELLED)) {
                    listener.onError(SpeechRecognizer.ERROR_CLIENT);
                } else {
                    listener.onError(SpeechRecognizer.ERROR_NETWORK);
                }

            } else {
                listener.onError(SpeechRecognizer.ERROR_NETWORK);
            }

            Recognition.setState(Recognition.State.IDLE);

            tidyUp();
        }
    };

    /**
     * Signal to the garbage collector
     */
    private void tidyUp() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "tidyUp");
        }

        // Signal to clean up
        System.gc();
    }

    /**
     * Controls how long the user can pause for before end of speech is detected
     *
     * @param detectionType one of {@link DetectionType#None} {@link DetectionType#Short} {@link DetectionType#Long}
     * @return the {@link DetectionType}
     */
    private DetectionType getDetectionType(final int detectionType) {

        switch (detectionType) {
            case 0:
                return DetectionType.None;
            case 1:
                return DetectionType.Short;
            case 2:
                return DetectionType.Long;
            default:
                return DetectionType.Long;
        }
    }
}

