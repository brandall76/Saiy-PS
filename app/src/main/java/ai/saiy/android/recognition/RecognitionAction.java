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

package ai.saiy.android.recognition;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.command.helper.CommandRequest;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.processing.Quantum;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;

/**
 * Class that determines where to send the recognition results.
 * <p/>
 * Created by benrandall76@gmail.com on 09/02/2016.
 */
public class RecognitionAction {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = RecognitionAction.class.getSimpleName();

    private final Context mContext;
    private final ArrayList<String> resultsRecognition;
    private final float[] confidenceScores;
    private final Locale vrLocale;
    private final Locale ttsLocale;
    private final SupportedLanguage sl;
    private final Bundle bundle;

    /**
     * Constructor.
     *
     * @param mContext  the application context
     * @param vrLocale  the voice recognition {@link Locale}
     * @param ttsLocale the Text to Speech {@link Locale}
     */
    public RecognitionAction(@NonNull final Context mContext, @NonNull final Locale vrLocale,
                             @NonNull final Locale ttsLocale, @NonNull final SupportedLanguage sl,
                             @NonNull final Bundle bundle) {
        this.mContext = mContext.getApplicationContext();
        this.bundle = bundle;

        this.resultsRecognition = this.bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        this.confidenceScores = this.bundle.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
        this.vrLocale = vrLocale;
        this.ttsLocale = ttsLocale;
        this.sl = sl;

        resolve();
    }

    /**
     * Resolve the command and check for other conditions such as conversation or root commands.
     * <p/>
     * Will either forward to {@link Quantum} or start an error response.
     * <p/>
     * The instance of {@link Quantum} should never be requested to run more than once and in
     * theory shouldn't be possible. For the sake of weird and wonderful error handling, we will use
     * an {@link AsyncTask#THREAD_POOL_EXECUTOR} so even if an instance is running, a new instance
     * is started in parallel to prevent a bottle-neck. This could produce some undesired side-effects
     * though.
     */
    @SuppressWarnings("unchecked")
    private void resolve() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "resolve");
        }

        if (UtilsList.notNaked(resultsRecognition)) {

            final CommandRequest cr = new CommandRequest(vrLocale, ttsLocale, sl);
            cr.setResultsArray(resultsRecognition);
            cr.setConfidenceArray(confidenceScores);
            cr.setWasSecure(bundle.getBoolean(RecognizerIntent.EXTRA_SECURE, false));

            final Quantum quantum = new Quantum(mContext);

            switch (bundle.getInt(LocalRequest.EXTRA_CONDITION, Condition.CONDITION_NONE)) {

                case Condition.CONDITION_NONE:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Condition.CONDITION_NONE");
                    }

                    quantum.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cr);
                    break;
                case Condition.CONDITION_CONVERSATION:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Condition.CONDITION_CONVERSATION");
                    }
                    // TODO
                    break;
                case Condition.CONDITION_ROOT:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Condition.CONDITION_ROOT");
                    }
                    // TODO
                    break;
                case Condition.CONDITION_TRANSLATION:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Condition.CONDITION_TRANSLATION");
                    }
                    // TODO
                    break;
                case Condition.CONDITION_USER_CUSTOM:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Condition.CONDITION_USER_CUSTOM");
                    }
                    // TODO
                    break;
                case Condition.CONDITION_EMOTION:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Condition.CONDITION_EMOTION");
                    }
                    // TODO
                    break;
                case Condition.CONDITION_IDENTITY:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Condition.CONDITION_IDENTITY");
                    }
                    // TODO
                    break;
                case Condition.CONDITION_IDENTIFY:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Condition.CONDITION_IDENTIFY");
                    }
                    // TODO
                    break;
                case Condition.CONDITION_GOOGLE_NOW:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Condition.CONDITION_GOOGLE_NOW");
                    }
                    // TODO - this is in danger of creating multiple instances

                    quantum.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cr);
                    break;
                case Condition.CONDITION_IGNORE:
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "Condition.CONDITION_IGNORE");
                    }
                    sendErrorLocalRequest();
                    break;
                default:
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "Condition.default");
                    }
                    sendErrorLocalRequest();
                    break;
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "recognition results naked");
            }
            sendErrorLocalRequest();
        }
    }

    /**
     * Utility method to notify the user of a processing error
     */
    private void sendErrorLocalRequest() {
        final LocalRequest lr = new LocalRequest(mContext);
        lr.setAction(LocalRequest.ACTION_SPEAK_ONLY);
        lr.setUtterance(PersonalityResponse.getErrorEmptyVoiceData(mContext, sl));
        lr.setTTSLocale(ttsLocale);
        lr.setVRLocale(vrLocale);
        lr.setSupportedLanguage(sl);
        lr.execute();
    }
}
