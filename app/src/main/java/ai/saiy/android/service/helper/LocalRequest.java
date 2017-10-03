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

package ai.saiy.android.service.helper;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.api.request.SaiyRequestParams;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.personality.PersonalityHelper;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.recognition.helper.RecognitionDefaults;
import ai.saiy.android.service.SelfAware;
import ai.saiy.android.service.ServiceConnector;
import ai.saiy.android.tts.helper.SpeechPriority;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsLocale;
import ai.saiy.android.utils.UtilsString;

/**
 * Helper Class to prepare our request to the {@link ServiceConnector}
 * <p/>
 * Created by benrandall76@gmail.com on 08/02/2016.
 */
public class LocalRequest {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = LocalRequest.class.getSimpleName();

    public static final int ACTION_UNKNOWN = 0;
    public static final int ACTION_SPEAK_ONLY = 1;
    public static final int ACTION_SPEAK_LISTEN = 2;
    public static final int ACTION_START_HOTWORD = 3;
    public static final int ACTION_STOP_HOTWORD = 4;
    public static final int ACTION_TOGGLE_HOTWORD = 5;

    public static final String EXTRA_RECOGNITION_LANGUAGE = "extra_recognition_language";
    public static final String EXTRA_TTS_LANGUAGE = "extra_tts_language";
    public static final String EXTRA_UTTERANCE = "extra_utterance";
    public static final String EXTRA_UTTERANCE_ARRAY = "extra_utterance_array";
    public static final String EXTRA_ACTION = "extra_action";
    public static final String EXTRA_COMMAND = "extra_command";
    public static final String EXTRA_CONDITION = "extra_condition";
    public static final String EXTRA_SUPPORTED_LANGUAGE = "extra_supported_language";
    public static final String EXTRA_QUEUE_TYPE = "extra_queue_type";
    public static final String EXTRA_PROFILE_ID = "extra_profile_id";
    public static final String EXTRA_SPEECH_PRIORITY = "extra_speech_priority";
    public static final String EXTRA_PREVENT_RECOGNITION = "extra_prevent_recognition";

    private final ServiceConnector sc;
    private final WeakReference<Context> weakContext;
    private final Bundle bundle;

    /**
     * Constructor.
     *
     * @param mContext the application context
     * @param bundle   contains additional parameters
     */
    public LocalRequest(@NonNull final Context mContext, @NonNull final Bundle bundle) {
        this.weakContext = new WeakReference<>(mContext.getApplicationContext());
        this.bundle = bundle;

        sc = new ServiceConnector(this.weakContext.get(), this);
    }

    /**
     * Constructor.
     *
     * @param mContext the application context
     */
    public LocalRequest(@NonNull final Context mContext) {
        this.weakContext = new WeakReference<>(mContext.getApplicationContext());
        this.bundle = new Bundle();

        sc = new ServiceConnector(this.weakContext.get(), this);
    }

    /**
     * Start the service connection
     */
    public void execute() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "execute");
        }

        sc.createConnection();
    }

    /**
     * Utility method to cut down on boiler plate requests
     */
    public void prepareIntro() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "prepareIntro");
        }

        final Locale vrLocale = SPH.getVRLocale(weakContext.get());
        final SupportedLanguage sl = SupportedLanguage.getSupportedLanguage(vrLocale);

        bundle.putInt(LocalRequest.EXTRA_ACTION, LocalRequest.ACTION_SPEAK_LISTEN);
        bundle.putString(LocalRequest.EXTRA_UTTERANCE, PersonalityHelper.getIntro(weakContext.get(), sl));
        bundle.putString(LocalRequest.EXTRA_RECOGNITION_LANGUAGE, vrLocale.toString());
        bundle.putString(LocalRequest.EXTRA_TTS_LANGUAGE, SPH.getTTSLocale(weakContext.get()).toString());
        bundle.putSerializable(LocalRequest.EXTRA_SUPPORTED_LANGUAGE, sl);
    }

    /**
     * Utility method to cut down on boiler plate requests
     *
     * @param sl        the {@link SupportedLanguage} object
     * @param vrLocale  the voice recognition {@link Locale}
     * @param ttsLocale the text to speech {@link Locale}
     */
    public void prepareCancelled(@NonNull final SupportedLanguage sl, @NonNull final Locale vrLocale,
                                 @NonNull final Locale ttsLocale) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "prepareCancelled");
        }

        bundle.putInt(LocalRequest.EXTRA_ACTION, LocalRequest.ACTION_SPEAK_ONLY);
        bundle.putString(LocalRequest.EXTRA_UTTERANCE, PersonalityResponse.getCancelled(weakContext.get(), sl));
        bundle.putString(LocalRequest.EXTRA_RECOGNITION_LANGUAGE, vrLocale.toString());
        bundle.putString(LocalRequest.EXTRA_TTS_LANGUAGE, ttsLocale.toString());
        bundle.putSerializable(LocalRequest.EXTRA_SUPPORTED_LANGUAGE, sl);
    }

    /**
     * Utility method to cut down on boiler plate requests
     *
     * @param action    to perform
     * @param utterance to speak
     */
    public void prepareDefault(final int action, @Nullable final String utterance) {

        final Locale vrLocale = SPH.getVRLocale(weakContext.get());
        final SupportedLanguage sl = SupportedLanguage.getSupportedLanguage(vrLocale);

        bundle.putInt(LocalRequest.EXTRA_ACTION, action);
        bundle.putString(LocalRequest.EXTRA_UTTERANCE, utterance);
        bundle.putString(LocalRequest.EXTRA_RECOGNITION_LANGUAGE, vrLocale.toString());
        bundle.putString(LocalRequest.EXTRA_TTS_LANGUAGE, SPH.getTTSLocale(weakContext.get()).toString());
        bundle.putSerializable(LocalRequest.EXTRA_SUPPORTED_LANGUAGE, sl);
    }

    /**
     * Utility method to cut down on boiler plate requests
     *
     * @param action    to perform
     * @param utterance to speak
     */
    public void prepareDefault(final int action, @NonNull final SupportedLanguage sl, @NonNull final Locale vrLocale,
                               @NonNull final Locale ttsLocale, @Nullable final String utterance) {

        bundle.putInt(LocalRequest.EXTRA_ACTION, action);
        bundle.putString(LocalRequest.EXTRA_UTTERANCE, utterance);
        bundle.putString(LocalRequest.EXTRA_RECOGNITION_LANGUAGE, vrLocale.toString());
        bundle.putString(LocalRequest.EXTRA_TTS_LANGUAGE, ttsLocale.toString());
        bundle.putSerializable(LocalRequest.EXTRA_SUPPORTED_LANGUAGE, sl);
    }

    /**
     * Get the standard request intent
     *
     * @return the standard {@link Intent}
     */
    public Intent getRequestIntent() {
        final Intent intent = new Intent(weakContext.get(), SelfAware.class);
        intent.setAction(weakContext.get().getPackageName());
        return intent;
    }

    /**
     * Get the required Locale of the Recognition Engine
     *
     * @return the required {@link Locale}
     */
    public Locale getVRLocale() {
        if (bundle != null) {
            if (bundle.containsKey(EXTRA_RECOGNITION_LANGUAGE)) {
                final String language = bundle.getString(EXTRA_RECOGNITION_LANGUAGE);
                if (UtilsString.notNaked(language)) {
                    return UtilsLocale.stringToLocale(language);
                }
            }
        }

        return SPH.getVRLocale(weakContext.get());
    }

    /**
     * Set the request Voice Recognition language
     *
     * @param loc the {@link Locale} of the request
     */
    public void setVRLocale(final Locale loc) {
        bundle.putString(EXTRA_RECOGNITION_LANGUAGE, loc.toString());

    }

    /**
     * Get the required Locale of the Text to Speech Engine
     *
     * @return the required {@link Locale}
     */
    public Locale getTTSLocale() {
        if (bundle != null) {
            if (bundle.containsKey(EXTRA_TTS_LANGUAGE)) {
                final String language = bundle.getString(EXTRA_TTS_LANGUAGE);
                if (UtilsString.notNaked(language)) {
                    return UtilsLocale.stringToLocale(language);
                }
            }
        }

        return SPH.getVRLocale(weakContext.get());
    }

    /**
     * Set the request Text to Speech language
     *
     * @param loc the {@link Locale} of the request
     */
    public void setTTSLocale(final Locale loc) {
        bundle.putString(EXTRA_TTS_LANGUAGE, loc.toString());

    }

    /**
     * Set the request action
     *
     * @param action one of {@link #ACTION_SPEAK_ONLY} {@link #ACTION_SPEAK_LISTEN}
     */
    public void setAction(final int action) {
        bundle.putInt(EXTRA_ACTION, action);
    }

    /**
     * Get the request action
     *
     * @return one of {@link #ACTION_SPEAK_ONLY} {@link #ACTION_SPEAK_LISTEN} or the error default
     * of {@link #ACTION_UNKNOWN}
     */
    public int getAction() {
        if (bundle != null) {
            if (bundle.containsKey(EXTRA_ACTION)) {
                return bundle.getInt(EXTRA_ACTION, ACTION_UNKNOWN);
            }
        }

        return ACTION_UNKNOWN;
    }

    /**
     * Set the request condition
     *
     * @param condition one of {@link Condition}
     */
    public void setCondition(final int condition) {
        bundle.putInt(EXTRA_CONDITION, condition);
    }

    /**
     * Get the request action
     *
     * @return one of {@link Condition}
     */
    public int getCondition() {
        if (bundle != null) {
            if (bundle.containsKey(EXTRA_CONDITION)) {
                return bundle.getInt(EXTRA_CONDITION, Condition.CONDITION_NONE);
            }
        }

        return Condition.CONDITION_NONE;
    }

    /**
     * Set the resolved command type
     *
     * @param command one of {@link CC}
     */
    public void setCommand(final CC command) {
        bundle.putSerializable(EXTRA_COMMAND, command);
    }

    /**
     * Get the command type
     *
     * @return one of {@link CC} or the unknown default
     * of {@link CC#COMMAND_UNKNOWN}
     */
    public CC getCommand() {
        if (bundle != null) {
            if (bundle.containsKey(EXTRA_COMMAND)) {
                return (CC) bundle.getSerializable(EXTRA_COMMAND);
            }
        }

        return CC.COMMAND_UNKNOWN;
    }

    /**
     * Set the {@link ArrayList<String>} of voice data
     *
     * @param voiceData array
     */
    public void setUtteranceArray(@NonNull final ArrayList<String> voiceData) {
        bundle.putStringArrayList(EXTRA_UTTERANCE_ARRAY, voiceData);
    }

    /**
     * Get the {@link ArrayList<String>} of voice data
     *
     * @return the voice data array or an default empty one
     */
    public ArrayList<String> getUtteranceArray() {
        if (bundle != null) {
            if (bundle.containsKey(EXTRA_UTTERANCE_ARRAY)) {
                final ArrayList<String> wordsArray = bundle.getStringArrayList(EXTRA_UTTERANCE_ARRAY);
                if (wordsArray != null) {
                    return wordsArray;
                }
            }
        }

        return new ArrayList<>();
    }

    /**
     * Set the utterance inside the instruction {@link Bundle}
     *
     * @param utterance to set
     */
    public void setUtterance(@NonNull final String utterance) {
        bundle.putString(EXTRA_UTTERANCE, utterance);
    }

    /**
     * Get the speech utterance from the request
     *
     * @return the {@link String} utterance or the default of {@link SaiyRequestParams#SILENCE}
     */
    public String getUtterance() {
        if (bundle != null) {
            if (bundle.containsKey(EXTRA_UTTERANCE)) {
                final String utterance = bundle.getString(EXTRA_UTTERANCE);
                if (UtilsString.notNaked(utterance)) {
                    return utterance;
                }
            }
        }

        return SaiyRequestParams.SILENCE;
    }

    /**
     * Set that the hotword detection should shutdown
     */
    public void setShutdownHotword() {
        bundle.putInt(RecognitionDefaults.HOTWORD_CONDITION, RecognitionDefaults.SHUTDOWN_HOTWORD);
    }

    /**
     * Get if the hotword detection should shutdown
     *
     * @return true if the hotword detection should be shutdown, false otherwise
     */
    public boolean getShutdownHotword() {
        return bundle != null && (bundle.getInt(RecognitionDefaults.HOTWORD_CONDITION,
                Condition.CONDITION_NONE) == RecognitionDefaults.SHUTDOWN_HOTWORD);

    }

    /**
     * Set that the request must be handle in secure mode
     *
     * @param secure true if the request should be handled securely, false otherwise
     */
    public void setSecure(final boolean secure) {
        bundle.putBoolean(RecognizerIntent.EXTRA_SECURE, secure);
    }

    /**
     * Get if the request has been made in secure mode
     *
     * @return true if the request was made in secure mode, false otherwise
     */
    public boolean isSecure() {
        return bundle != null && bundle.getBoolean(RecognizerIntent.EXTRA_SECURE, false);
    }

    /**
     * Set the profile id for speaker recognition usage
     *
     * @param profileId the id of the enrolled profile
     */
    public void setIdentityProfile(@NonNull final String profileId) {
        bundle.putString(EXTRA_PROFILE_ID, profileId);
    }

    /**
     * Get the enrolled profile id
     *
     * @return the profile id or an empty string is one is not set
     */
    public String getIdentityProfile() {
        if (bundle != null) {
            return bundle.getString(EXTRA_PROFILE_ID, "");
        }
        return "";
    }

    /**
     * Get the {@link Bundle} of instructions
     *
     * @return the {@link Bundle} of instructions
     */
    public Bundle getBundle() {
        return this.bundle;
    }

    /**
     * Get the {@link SupportedLanguage}
     *
     * @return the {@link SupportedLanguage}
     */
    public SupportedLanguage getSupportedLanguage() {
        if (bundle.containsKey(EXTRA_SUPPORTED_LANGUAGE)) {
            return (SupportedLanguage) bundle.getSerializable(EXTRA_SUPPORTED_LANGUAGE);
        }

        final SupportedLanguage sl = SupportedLanguage.getSupportedLanguage(getVRLocale());
        bundle.putSerializable(EXTRA_SUPPORTED_LANGUAGE, sl);

        return sl;
    }

    public void setSupportedLanguage(@NonNull final SupportedLanguage sl) {
        bundle.putSerializable(EXTRA_SUPPORTED_LANGUAGE, sl);
    }

    /**
     * Set the Text to Speech queue type
     *
     * @param queueType one of {@link TextToSpeech#QUEUE_ADD} or {@link TextToSpeech#QUEUE_FLUSH}
     */
    public void setQueueType(final int queueType) {
        bundle.putInt(EXTRA_QUEUE_TYPE, queueType);
    }

    /**
     * Get the Text to Speech queue type
     *
     * @return one of {@link TextToSpeech#QUEUE_ADD} or {@link TextToSpeech#QUEUE_FLUSH}
     */
    public int getQueueType() {
        return bundle.getInt(EXTRA_QUEUE_TYPE, TextToSpeech.QUEUE_FLUSH);
    }

    public void setSpeechPriority(final int priority) {
        bundle.putInt(EXTRA_SPEECH_PRIORITY, priority);
    }

    public int getSpeechPriority() {
        return bundle == null ? SpeechPriority.PRIORITY_NORMAL
                : bundle.getInt(EXTRA_SPEECH_PRIORITY, SpeechPriority.PRIORITY_NORMAL);
    }

    public void setPreventRecognition() {
        bundle.putBoolean(EXTRA_PREVENT_RECOGNITION, true);
    }

    public boolean shouldPreventRecognition() {
        return bundle != null && bundle.getBoolean(EXTRA_PREVENT_RECOGNITION, false);
    }
}
