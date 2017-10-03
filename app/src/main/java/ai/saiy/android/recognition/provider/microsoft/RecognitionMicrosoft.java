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

package ai.saiy.android.recognition.provider.microsoft;

import android.content.Context;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.microsoft.bing.speech.SpeechClientStatus;
import com.microsoft.projectoxford.speechrecognition.Confidence;
import com.microsoft.projectoxford.speechrecognition.ISpeechRecognitionServerEvents;
import com.microsoft.projectoxford.speechrecognition.MicrophoneRecognitionClient;
import com.microsoft.projectoxford.speechrecognition.RecognitionResult;
import com.microsoft.projectoxford.speechrecognition.SpeechRecognitionMode;
import com.microsoft.projectoxford.speechrecognition.SpeechRecognitionServiceFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.api.SaiyDefaults;
import ai.saiy.android.api.language.nlu.NLULanguageMicrosoft;
import ai.saiy.android.api.language.vr.VRLanguageMicrosoft;
import ai.saiy.android.api.remote.Request;
import ai.saiy.android.audio.SaiySoundPool;
import ai.saiy.android.configuration.MicrosoftConfiguration;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.nlu.microsoft.ResolveMicrosoft;
import ai.saiy.android.recognition.Recognition;
import ai.saiy.android.recognition.SaiyRecognitionListener;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsLocale;


/**
 * Class uses the Project Oxford speech SDK. At the time of writing, the initialisation is too slow
 * to be usable in production.
 * <p/>
 * Register at <a href="https://www.microsoft.com/cognitive-services/en-us/sign-up">Microsoft Cognitive Services</a>
 * to get 2 API keys and enter them in {@link MicrosoftConfiguration}
 * <p/>
 * Created by benrandall76@gmail.com on 18/04/2016.
 */
public class RecognitionMicrosoft implements ISpeechRecognitionServerEvents {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = RecognitionMicrosoft.class.getSimpleName();

    private final Bundle partialBundle = new Bundle();
    private final Bundle resultsBundle = new Bundle();
    private final ArrayList<String> partialArray = new ArrayList<>();

    private final SaiySoundPool ssp;

    private final Context mContext;
    private final MicrophoneRecognitionClient client;
    private final SaiyRecognitionListener listener;
    private final SaiyDefaults.LanguageModel languageModel;
    private final Locale ttsLocale;
    private final VRLanguageMicrosoft vrLocale;
    private final SupportedLanguage sl;
    private final boolean servingRemote;

    private boolean error;

    private final ArrayList<String> resultsArray = new ArrayList<>();
    private float[] floatsArray = null;


    /**
     * Constructor
     *
     * @param mContext             the application context
     * @param listener             the associated {@link SaiyRecognitionListener}
     * @param apiKey1              the Project Oxford key 1
     * @param apiKey2              the Project Oxford key 2
     * @param appId                the LUIS app id
     * @param subscriptionId       the LUIS subscription id
     * @param languageModel        the {@link SaiyDefaults.LanguageModel}
     * @param ttsLocale            the Text to Speech {@link Locale}
     * @param vrLocale             the {@link VRLanguageMicrosoft}
     * @param nluLanguageMicrosoft the {@link NLULanguageMicrosoft}
     * @param sl                   the {@link SupportedLanguage}
     * @param servingRemote        true if the origin is a remote request
     */
    public RecognitionMicrosoft(@NonNull final Context mContext, @NonNull final SaiyRecognitionListener listener,
                                @NonNull final String apiKey1,
                                @NonNull final String apiKey2, @NonNull final String appId,
                                @NonNull final String subscriptionId,
                                @NonNull final SaiyDefaults.LanguageModel languageModel,
                                @NonNull final Locale ttsLocale, @NonNull final VRLanguageMicrosoft vrLocale,
                                @Nullable final NLULanguageMicrosoft nluLanguageMicrosoft,
                                @NonNull final SupportedLanguage sl,
                                final boolean servingRemote, final SaiySoundPool ssp) {
        this.mContext = mContext;
        this.listener = listener;
        this.languageModel = languageModel;
        this.ttsLocale = ttsLocale;
        this.vrLocale = vrLocale;
        this.sl = sl;
        this.servingRemote = servingRemote;
        this.ssp = ssp;

        error = false;

        if (DEBUG) {
            MyLog.i(CLS_NAME, "language: " + vrLocale.getLocaleString());
        }

        if (languageModel != SaiyDefaults.LanguageModel.MICROSOFT) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "nlu not required");
            }

            client = SpeechRecognitionServiceFactory.createMicrophoneClient(SpeechRecognitionMode.ShortPhrase,
                    vrLocale.getLocaleString(), this, apiKey1, apiKey2);
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "nlu required");
            }

            //noinspection ConstantConditions
            client = SpeechRecognitionServiceFactory.createMicrophoneClientWithIntent(
                    nluLanguageMicrosoft.getLocaleString(), this, apiKey1, apiKey2, appId, subscriptionId);

        }
    }

    /**
     * Start the recognition.
     */
    public void startListening() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "called startRecording");
        }
        client.startMicAndRecognition();
    }

    /**
     * Stop the recognition.
     */
    public void stopListening() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "called stopRecording");
        }

        Recognition.setState(Recognition.State.PROCESSING);

        if (client != null) {
            client.endMicAndRecognition();
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "called stopRecording: client null or cancelled");
            }
        }
    }

    @Override
    public void onPartialResponseReceived(final String partial) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onPartialResponseReceived: " + partial);
        }

        partialArray.clear();
        partialBundle.clear();

        partialArray.add(partial);
        partialBundle.putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, partialArray);
        listener.onPartialResults(partialBundle);
    }

    @Override
    public void onFinalResponseReceived(final RecognitionResult response) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onFinalResponseReceived: " + response.RecognitionStatus.name());
        }

        switch (response.RecognitionStatus) {

            case RecognitionSuccess:
                doSuccess(response);
                break;
            case NoMatch:
            case InitialSilenceTimeout:
                error = true;
                listener.onError(SpeechRecognizer.ERROR_NO_MATCH);
                break;
            case BabbleTimeout:
            case Intermediate:
            case HotWordMaximumTime:
            case Cancelled:
            case RecognitionError:
            case DictationEndSilenceTimeout:
            case EndOfDictation:
            case None:
                error = true;
                listener.onError(SpeechRecognizer.ERROR_CLIENT);
                break;
        }

        if (client != null) {
            client.endMicAndRecognition();
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onFinalResponseReceived: client null");
            }
        }
    }

    /**
     * Process the {@link RecognitionResult}
     *
     * @param response the {@link RecognitionResult}
     */
    private void doSuccess(final RecognitionResult response) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "doSuccess");
        }

        resultsArray.clear();
        resultsBundle.clear();

        floatsArray = new float[response.Results.length];

        for (int i = 0; i < response.Results.length; i++) {
            floatsArray[i] = convertConfidence(response.Results[i].Confidence);

            if (response.Results[i].DisplayText.endsWith(".")) {
                resultsArray.add(response.Results[i].DisplayText.substring(0,
                        response.Results[i].DisplayText.length() - 1));
            } else {
                resultsArray.add(response.Results[i].DisplayText);
            }
        }

        resultsBundle.putFloatArray(SpeechRecognizer.CONFIDENCE_SCORES, floatsArray);
        resultsBundle.putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, resultsArray);

        if (languageModel != SaiyDefaults.LanguageModel.MICROSOFT) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "nlu not required: triggering onResults");
            }
            listener.onResults(resultsBundle);
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "nlu required: holding results");
            }
        }

        Recognition.setState(Recognition.State.IDLE);
    }

    @Override
    public void onIntentReceived(final String payload) {
        if (DEBUG) {
            try {
                MyLog.i(CLS_NAME, "onIntentReceived");
                MyLog.i(CLS_NAME, "onIntentReceived: " + new JSONObject(payload).toString(4));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (languageModel == SaiyDefaults.LanguageModel.MICROSOFT) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onIntentReceived: nlu required");
            }

            if (servingRemote) {
                resultsBundle.putString(Request.RESULTS_NLU, payload);
                listener.onResults(resultsBundle);
            } else {
                new ResolveMicrosoft(mContext, sl, UtilsLocale.stringToLocale(vrLocale.getLocaleString()),
                        ttsLocale, floatsArray, resultsArray).unpack(payload);
            }
        }
    }

    @Override
    public void onError(final int errorCode, final String errorString) {
        if (DEBUG) {
            MyLog.w(CLS_NAME, "onError: " + errorString);
            MyLog.w(CLS_NAME, "onError: " + SpeechClientStatus.fromInt(errorCode) + " " + errorCode);
        }
        error = true;
        listener.onError(errorCode);
    }

    @Override
    public void onAudioEvent(final boolean recording) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onAudioEvent: recording: " + recording);
        }

        if (recording) {
            listener.onReadyForSpeech(null);
            listener.onBeginningOfSpeech();
            ssp.play(ssp.getBeepStart());
        } else {
            ssp.play(ssp.getBeepStop());
            client.endMicAndRecognition();

            if (!error) {
                listener.onEndOfSpeech();
            }
        }
    }

    /**
     * Convert the {@link Confidence} enum into an equivalent float value
     *
     * @param confidence the {@link Confidence}
     * @return the equivalent float value
     */
    private float convertConfidence(@NonNull final Confidence confidence) {

        switch (confidence) {
            case None:
                return 0.1f;
            case Low:
                return 0.3f;
            case Normal:
                return 0.6f;
            case High:
                return 0.9f;
            default:
                return 0.1f;
        }
    }
}
