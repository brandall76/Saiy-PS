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

package ai.saiy.android.tts;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;
import android.widget.Toast;

import com.google.gson.GsonBuilder;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IllformedLocaleException;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.regex.Pattern;

import ai.saiy.android.R;
import ai.saiy.android.api.request.SaiyRequestParams;
import ai.saiy.android.audio.AudioCompression;
import ai.saiy.android.audio.SaiyAudioTrack;
import ai.saiy.android.cache.speech.SpeechCacheResult;
import ai.saiy.android.database.DBSpeech;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.service.helper.SelfAwareCache;
import ai.saiy.android.service.helper.SelfAwareConditions;
import ai.saiy.android.service.helper.SelfAwareHelper;
import ai.saiy.android.service.helper.SelfAwareParameters;
import ai.saiy.android.tts.attributes.Gender;
import ai.saiy.android.tts.helper.SaiyVoice;
import ai.saiy.android.tts.helper.TTSDefaults;
import ai.saiy.android.tts.sound.SoundEffect;
import ai.saiy.android.tts.sound.SoundEffectHelper;
import ai.saiy.android.tts.sound.SoundEffectItem;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsLocale;
import ai.saiy.android.utils.UtilsString;

/**
 * Due to misbehaving voice engines, it's necessary to subclass the TTS object here and handle extra
 * eventualities that have caused many crashes along the way.
 * <p/>
 * Additionally, handling the try/catch inside the methods keeps other classes tidy and more readable.
 * <p/>
 * Created by benrandall76@gmail.com on 13/03/2016.
 */
public class SaiyTextToSpeech extends TextToSpeech {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = SaiyTextToSpeech.class.getSimpleName();

    public static final int MAX_UTTERANCE_LENGTH = 500;
    public static final int MIN_UTTERANCE_LENGTH = 200;

    private static final String ARRAY = "array";
    public static final String ARRAY_FIRST = "array_first";
    public static final String ARRAY_INTERIM = "array_interim";
    public static final String ARRAY_LAST = "array_last";
    public static final String ARRAY_SINGLE = "array_single";
    public static final String ARRAY_DELIMITER = "~~";

    private volatile SaiyAudioTrack audioTrack;

    private volatile SaiyProgressListener listener;
    private volatile String initEngine;
    private volatile Set<SaiyVoice> saiyVoiceSet;
    private volatile Set<Voice> defaultVoiceSet;

    private final Context mContext;

    /**
     * Constructor
     *
     * @param mContext the application context
     * @param listener the {@link OnInitListener} to monitor the tts engine
     */
    public SaiyTextToSpeech(final Context mContext, final OnInitListener listener) {
        super(mContext, listener);
        this.mContext = mContext;
        init();
    }

    /**
     * Constructor
     *
     * @param mContext the application context
     * @param listener the {@link OnInitListener} to monitor the tts engine
     * @param engine   package name of a requested engine
     */
    public SaiyTextToSpeech(final Context mContext, final OnInitListener listener, final String engine) {
        super(mContext, listener, engine);
        this.mContext = mContext;
        init();
    }

    /**
     * Set post constructor stuff up
     */
    private void init() {
        initialiseAudioTrack();
        setAttributes();
    }

    /**
     * Set the standard audio attributes for the Text to Speech stream
     */
    private void setAttributes() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setAudioAttributes(new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                    .setLegacyStreamType(AudioManager.STREAM_MUSIC).build());
        }
    }

    /**
     * Initialise our {@link SaiyAudioTrack} object, which will be used for streaming stored utterances.
     */
    private void initialiseAudioTrack() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            audioTrack = getAudioTrack();
        }
    }

    /**
     * Helper method to double check the returned {@link SaiyAudioTrack} object hasn't been released
     * elsewhere.
     *
     * @return the {@link SaiyAudioTrack} object, or null it the creation process failed.
     */
    private SaiyAudioTrack getAudioTrack() {
        if (audioTrack == null || audioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
            audioTrack = SaiyAudioTrack.getSaiyAudioTrack();
            audioTrack.setListener(listener);
            return audioTrack;
        } else {
            return audioTrack;
        }
    }

    /**
     * Process a Text to Speech utterance request, considering API versions, sound effects, silence
     * and other possibilities.
     *
     * @param text        the utterance
     * @param queueMode   one of {@link #QUEUE_ADD} or {@link #QUEUE_FLUSH}
     * @param params      the {@link SelfAwareParameters} object
     * @param utteranceId the utterance id
     * @return one of {@link #SUCCESS} or {@link Error}
     */
    public int speak(@NonNull CharSequence text, final int queueMode,
                     @NonNull final SelfAwareParameters params, @NonNull final String utteranceId) {

        if (SoundEffectHelper.pSOUND_EFFECT.matcher(text).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "speak: have sound effect");
            }

            final Gender gender;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                final SaiyVoice voice = getBoundSaiyVoice();

                if (voice != null) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "speak: have sound effect: voice.getGender(): " + voice.getGender().name());
                    }
                    gender = voice.getGender();
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "speak: have sound effect: voice null");
                    }
                    gender = Gender.UNDEFINED;
                }
            } else {
                gender = Gender.UNDEFINED;
            }

            final SoundEffectHelper helper = new SoundEffectHelper(text, utteranceId, gender);
            helper.sort();
            final ArrayList<SoundEffectItem> items = helper.getArray();
            final ArrayList<String> addedItems = SoundEffectHelper.getAddedItems();

            int result = ERROR;
            for (final SoundEffectItem item : items) {

                params.setUtteranceId(item.getUtteranceId());

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "item getText: " + item.getText());
                    MyLog.i(CLS_NAME, "item getItemType: " + item.getItemType());
                    MyLog.i(CLS_NAME, "item getUtteranceId: " + item.getUtteranceId());
                }

                switch (item.getItemType()) {

                    case SoundEffectItem.SOUND:

                        if (addedItems.contains(item.getText())) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                result = playEarcon(item.getText(), QUEUE_ADD, params.getBundle(), item.getUtteranceId());
                            } else {
                                result = playEarcon(item.getText(), QUEUE_ADD, params);
                            }
                        } else {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "No valid sound effect: " + item.getText());
                            }
                            result = ERROR;
                        }

                        break;
                    case SoundEffectItem.SPEECH:

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            result = speak21(item.getText(), QUEUE_ADD, params, item.getUtteranceId());
                        } else {
                            result = speak(item.getText(), QUEUE_ADD, params);
                        }
                        break;
                    case SoundEffectItem.SILENCE:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            result = playSilentUtterance(SoundEffect.SILENCE_DURATION, QUEUE_ADD,
                                    item.getUtteranceId());
                        } else {
                            result = playSilence(SoundEffect.SILENCE_DURATION, QUEUE_ADD, params);
                        }
                        break;
                }
            }

            return result;
        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return speak21(text, queueMode, params, utteranceId);
            } else {
                return speak(text.toString(), queueMode, params);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private int speak21(@NonNull final CharSequence text, final int queueMode,
                        @NonNull final SelfAwareParameters params, @NonNull final String utteranceId) {

        if (queueMode != QUEUE_ADD && !params.getUtteranceId().startsWith(ARRAY)
                && canSynthesise(text.toString(), params)) {
            return SUCCESS;
        } else {
            if (text.length() > getMaxUtteranceLength()) {

                final ArrayList<String> splitUtterances = SelfAwareHelper.splitUtteranceRegex(
                        text.toString(), getMaxUtteranceLength());

                final int splitUtterancesSize = splitUtterances.size();

                if (splitUtterancesSize > 1) {

                    final boolean overrideId = utteranceId.contains(ARRAY_DELIMITER);

                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "speak21: SU: " + utteranceId);
                        MyLog.i(CLS_NAME, "speak21: SU override: " + overrideId);
                    }

                    for (int i = 0; i < splitUtterancesSize; i++) {

                        final String resolvedUtteranceId = resolveUtteranceId(utteranceId, splitUtterancesSize,
                                i, overrideId);

                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "speak21: SU resolvedUtteranceId: " + resolvedUtteranceId);
                        }

                        params.setUtteranceId(resolvedUtteranceId);

                        if (i == (splitUtterancesSize - 1)) {
                            return super.speak(splitUtterances.get(i), QUEUE_ADD, params.getBundle(),
                                    params.getUtteranceId());
                        } else {
                            super.speak(splitUtterances.get(i), QUEUE_ADD, params.getBundle(),
                                    params.getUtteranceId());
                        }
                    }

                } else {
                    return super.speak(text, queueMode, params.getBundle(), utteranceId);
                }
            } else {
                return super.speak(text, queueMode, params.getBundle(), utteranceId);
            }
        }

        return super.speak(text, queueMode, params.getBundle(), utteranceId);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int speak(final String text, final int queueMode, final HashMap<String, String> map) {

        if (text.length() > getMaxUtteranceLength()) {

            final ArrayList<String> splitUtterances = SelfAwareHelper.splitUtteranceRegex(text,
                    getMaxUtteranceLength());

            final int splitUtterancesSize = splitUtterances.size();

            if (splitUtterancesSize > 1) {

                final String utteranceId = map.get(Engine.KEY_PARAM_UTTERANCE_ID);
                final boolean overrideId = utteranceId.contains(ARRAY_DELIMITER);

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "speak: SU: " + utteranceId);
                    MyLog.i(CLS_NAME, "speak: SU override: " + overrideId);
                }

                for (int i = 0; i < splitUtterancesSize; i++) {

                    final String resolvedUtteranceId = resolveUtteranceId(utteranceId, splitUtterancesSize,
                            i, overrideId);

                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "speak: SU resolvedUtteranceId: " + resolvedUtteranceId);
                    }

                    map.put(Engine.KEY_PARAM_UTTERANCE_ID, resolvedUtteranceId);

                    if (i == (splitUtterancesSize - 1)) {
                        return super.speak(splitUtterances.get(i), QUEUE_ADD, map);
                    } else {
                        super.speak(splitUtterances.get(i), QUEUE_ADD, map);
                    }
                }

            } else {
                return super.speak(text, queueMode, map);
            }
        } else {
            return super.speak(text, queueMode, map);
        }

        return super.speak(text, queueMode, map);
    }

    private String resolveUtteranceId(@NonNull final String utteranceId, final int arraySize, final int position,
                                      final boolean overrideId) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "resolveUtteranceId: utteranceId: " + utteranceId);
            MyLog.i(CLS_NAME, "resolveUtteranceId: arraySize: " + arraySize);
            MyLog.i(CLS_NAME, "resolveUtteranceId: position: " + position);
            MyLog.i(CLS_NAME, "resolveUtteranceId: overrideId: " + overrideId);
        }

        if (position == (arraySize - 1)) {

            if (overrideId) {

                if (utteranceId.startsWith(ARRAY_INTERIM) || utteranceId.startsWith(ARRAY_LAST)) {
                    return utteranceId;
                } else {
                    return ARRAY_INTERIM + ARRAY_DELIMITER + TextUtils.split(utteranceId, ARRAY_DELIMITER)[1];
                }

            } else {
                return ARRAY_LAST + ARRAY_DELIMITER + utteranceId;
            }

        } else {

            switch (position) {

                case 0:

                    if (overrideId) {

                        if (utteranceId.startsWith(ARRAY_FIRST) || utteranceId.startsWith(ARRAY_INTERIM)) {
                            return utteranceId;
                        } else {
                            return ARRAY_INTERIM + ARRAY_DELIMITER + TextUtils.split(utteranceId, ARRAY_DELIMITER)[1];
                        }

                    } else {
                        return ARRAY_FIRST + ARRAY_DELIMITER + utteranceId;
                    }

                default:

                    if (overrideId) {

                        if (utteranceId.startsWith(ARRAY_INTERIM)) {
                            return utteranceId;
                        } else {
                            return ARRAY_INTERIM + ARRAY_DELIMITER + TextUtils.split(utteranceId, ARRAY_DELIMITER)[1];
                        }

                    } else {
                        return ARRAY_INTERIM + ARRAY_DELIMITER + utteranceId;
                    }

            }
        }
    }

    /**
     * Method to check if synthesis exists in the {@link DBSpeech} for the pending utterance.
     *
     * @param utterance the pending utterance
     * @param voiceName the name of the current Text to Speech Voice
     * @return true if the audio data is available to stream. False otherwise.
     */
    private boolean synthesisAvailable(@NonNull final String utterance, @NonNull final String voiceName) {

        final DBSpeech dbSpeech = new DBSpeech(mContext);
        final SpeechCacheResult speechCacheResult = dbSpeech.getBytes(getInitialisedEngine(),
                voiceName, utterance);

        if (speechCacheResult.isSuccess()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "synthesisAvailable: true");
            }
            return true;
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "synthesisAvailable: getBytes failed or speech does not exist");
            }

            if (speechCacheResult.getRowId() > -1) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "synthesisAvailable: speech does not exist");
                }
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
                        dbSpeech.deleteEntry(speechCacheResult.getRowId());
                    }
                });
            }
        }

        return false;
    }

    /**
     * Method to check if synthesis exists in the {@link DBSpeech} for the pending utterance. If so,
     * the byte[] is pulled from the database and streamed using the {@link AudioTrack}, rather
     * than than via the Text to Speech engine.
     *
     * @param utterance the pending utterance
     * @param params    the {@link SelfAwareParameters}
     * @return true if the audio data is available to stream. False otherwise.
     */
    private boolean canSynthesise(@NonNull final String utterance, @NonNull final SelfAwareParameters params) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (getAudioTrack() != null) {
                if (TTSDefaults.isApprovedVoice(getInitialisedEngine())) {
                    if (utterance.length() < SelfAwareCache.MAX_UTTERANCE_CHARS) {
                        if (!utterance.matches(SaiyRequestParams.SILENCE)) {
                            if (UtilsString.notNaked(getInitialisedEngine())) {

                                final SaiyVoice voice = getBoundSaiyVoice();

                                if (voice != null) {

                                    final DBSpeech dbSpeech = new DBSpeech(mContext);
                                    final SpeechCacheResult speechCacheResult = dbSpeech.getBytes(getInitialisedEngine(),
                                            voice.getName(), utterance);

                                    if (speechCacheResult.isSuccess()) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "canSynthesise: true");
                                        }

                                        final byte[] uncompressedBytes = AudioCompression.decompressBytes(mContext,
                                                speechCacheResult.getCompressedBytes(), speechCacheResult.getRowId());

                                        if (UtilsList.notNaked(uncompressedBytes)) {
                                            startSynthesis(params, uncompressedBytes);
                                            return true;
                                        } else {
                                            if (DEBUG) {
                                                MyLog.i(CLS_NAME, "canSynthesise: getBytes empty or null");
                                            }
                                        }
                                    } else {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "canSynthesise: getBytes failed or speech does not exist");
                                        }

                                        if (speechCacheResult.getRowId() > -1) {
                                            AsyncTask.execute(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
                                                    dbSpeech.deleteEntry(speechCacheResult.getRowId());
                                                }
                                            });
                                        }
                                    }
                                } else {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "canSynthesise: false: saiyVoice null");
                                    }
                                }
                            } else {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "canSynthesise: false: getBytes engine naked");
                                }
                            }
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "canSynthesise: false: silence");
                            }
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "canSynthesise: false: length > MAX_UTTERANCE_CHARS");
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "canSynthesise: false: not approved engine");
                    }
                }
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "canSynthesise: false: audioTrack null");
                }
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "canSynthesise: false: < LOLLIPOP");
            }
        }

        return false;
    }

    /**
     * Begin streaming the byte[] of pcm audio data via the {@link AudioTrack} object
     *
     * @param uncompressedBytes to stream
     * @param params            the {@link SelfAwareParameters}
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startSynthesis(@NonNull final SelfAwareParameters params,
                                @NonNull final byte[] uncompressedBytes) {
        audioTrack.setListener(listener);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
                audioTrack.setVolume(params.getVolume());
                audioTrack.enqueue(uncompressedBytes, params.getUtteranceId());
            }
        });
    }

    /**
     * Add the inbuilt array of sound effect that can be played by the engine on demand
     */
    private void addSoundEffects() {

        final ArrayList<String> addedItems = new ArrayList<>();
        final TypedArray typedArray = mContext.getResources().obtainTypedArray(R.array.array_se);

        int temp;
        String fileName;
        for (int i = 0; i < typedArray.length(); i++) {

            temp = typedArray.getResourceId(i, -1);

            if (temp > 0) {

                try {

                    fileName = mContext.getResources().getResourceEntryName(temp);

                    if (DEBUG) {
                        MyLog.v(CLS_NAME, "addSoundEffects: fileName: " + fileName);
                    }

                    switch (addEarcon(fileName, mContext.getPackageName(), temp)) {

                        case SUCCESS:
                            if (DEBUG) {
                                MyLog.v(CLS_NAME, "addSoundEffects: fileName: SUCCESS: " + fileName);
                            }
                            addedItems.add(fileName);
                            break;
                        case ERROR:
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "addSoundEffects: fileName: ERROR: " + fileName);
                            }
                            break;
                    }

                } catch (final Resources.NotFoundException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "addSoundEffects: Resources.NotFoundException: " + temp);
                        e.printStackTrace();
                    }
                }
            }
        }

        typedArray.recycle();
        SoundEffectHelper.setAddedItems(addedItems);
    }

    @Override
    public int addEarcon(final String earcon, final File file) {
        return super.addEarcon(earcon, file);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int addEarcon(final String earcon, final String filename) {
        return super.addEarcon(earcon, filename);
    }

    @Override
    public int addEarcon(final String earcon, final String packageName, final int resourceId) {
        return super.addEarcon(earcon, packageName, resourceId);
    }

    @Override
    public int addSpeech(final CharSequence text, final File file) {
        return super.addSpeech(text, file);
    }

    @Override
    public int addSpeech(final CharSequence text, final String packageName,
                         final int resourceId) {
        return super.addSpeech(text, packageName, resourceId);
    }

    @Override
    public int addSpeech(final String text, final String filename) {
        return super.addSpeech(text, filename);
    }

    @Override
    public int addSpeech(final String text, final String packageName, final int resourceId) {
        return super.addSpeech(text, packageName, resourceId);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean areDefaultsEnforced() {
        return super.areDefaultsEnforced();
    }

    @Override
    public Set<Locale> getAvailableLanguages() {
        return super.getAvailableLanguages();
    }

    @Override
    public String getDefaultEngine() {

        String packageName = "";

        try {
            packageName = super.getDefaultEngine();
        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getDefaultEngineSecure: NullPointerException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getDefaultEngineSecure: Exception");
                e.printStackTrace();
            }
        }

        return packageName;
    }

    /**
     * for some TTS Engines, the {@link #getDefaultEngine()} is very slow - here we jump straight
     * to the Secure Settings to access it. If an exception is thrown, we revert to the latter.
     *
     * @return the user default TTS Engine
     */

    private String getDefaultEngineSecure() {

        try {

            final String packageName = Settings.Secure.getString(mContext.getContentResolver(),
                    Settings.Secure.TTS_DEFAULT_SYNTH);

            if (packageName != null) {
                if (DEBUG) {
                    MyLog.v(CLS_NAME, "getDefaultEngineSecure: Secure: " + packageName);
                }

                return packageName;
            }

        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getDefaultEngineSecure: NullPointerException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getDefaultEngineSecure: Exception");
                e.printStackTrace();
            }
        }

        return getDefaultEngine();
    }

    /**
     * Compare the current initialised TTS Engine with the user's default selection. If this has
     * changed since initialisation occurred, we need to restart the Engine, to bind to the new
     * default choice.
     *
     * @param packageName supplied if we want to compare a specific Engine
     * @return true if the default Engine does not match the initialised Engine
     */
    public boolean shouldReinitialise(final String packageName) {

        final String initialisedEngine = getInitialisedEngine();

        if (!UtilsString.notNaked(initialisedEngine)) {
            return true;
        }

        if (packageName != null) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "shouldReinitialise: comparing " + initialisedEngine
                        + " ~ " + packageName);
            }
            return !packageName.matches(initialisedEngine);
        } else {

            final String defaultEngine = getDefaultEngineSecure();

            if (DEBUG) {
                MyLog.v(CLS_NAME, "shouldReinitialise: comparing " + defaultEngine
                        + " ~ " + initialisedEngine);
            }

            return !UtilsString.notNaked(defaultEngine) || !defaultEngine.matches(initialisedEngine);

        }
    }

    /**
     * Check if the user has the Google TTS Engine installed
     *
     * @param ctx the application context
     * @return true if the Google TTS Engine is installed
     */
    public static boolean isGoogleTTSAvailable(final Context ctx) {
        try {
            ctx.getApplicationContext().getPackageManager().getApplicationInfo(TTSDefaults.TTS_PKG_NAME_GOOGLE, 0);
            return true;
        } catch (final PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * Limit of length of input string passed to speak and synthesizeToFile.
     *
     * @see #speak
     * @see #synthesizeToFile
     */
    private int getMaxUtteranceLength() {

        if (getInitialisedEngine().startsWith(TTSDefaults.TTS_PKG_NAME_GOOGLE)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getMaxUtteranceLength: TTS_PKG_NAME_GOOGLE");
            }
        } else if (getInitialisedEngine().startsWith(TTSDefaults.TTS_PKG_NAME_IVONA)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getMaxUtteranceLength: TTS_PKG_NAME_IVONA");
            }
        } else if (getInitialisedEngine().startsWith(TTSDefaults.TTS_PKG_NAME_CEREPROC)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getMaxUtteranceLength: TTS_PKG_NAME_CEREPROC");
            }
        } else if (getInitialisedEngine().startsWith(TTSDefaults.TTS_PKG_NAME_PICO)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getMaxUtteranceLength: TTS_PKG_NAME_PICO");
            }
        } else if (getInitialisedEngine().startsWith(TTSDefaults.TTS_PKG_NAME_SVOX)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getMaxUtteranceLength: TTS_PKG_NAME_SVOX");
            }
        }

        return MAX_UTTERANCE_LENGTH;
    }

    @SuppressWarnings("deprecation")
    @Override
    public Locale getDefaultLanguage() {
        return super.getDefaultLanguage();
    }

    @Override
    public List<EngineInfo> getEngines() {
        return super.getEngines();
    }

    @SuppressWarnings("deprecation")
    @Override
    public Set<String> getFeatures(final Locale locale) {
        return super.getFeatures(locale);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Locale getLanguage() {
        return super.getLanguage();
    }

    @Override
    public Voice getDefaultVoice() {
        return super.getDefaultVoice();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private SaiyVoice getUserDefaultSaiyVoice() {

        final String userDefaultSaiyVoiceString = SPH.getDefaultTTSVoice(mContext);

        if (UtilsString.notNaked(userDefaultSaiyVoiceString)) {

            final SaiyVoice userDefaultSaiyVoice = new GsonBuilder().disableHtmlEscaping().create().fromJson(
                    userDefaultSaiyVoiceString, SaiyVoice.class);

            if (userDefaultSaiyVoice != null) {
                return userDefaultSaiyVoice;
            }

        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "userDefaultSaiyVoiceString: naked");
            }
        }

        return null;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private SaiyVoice getEngineDefaultSaiyVoice() {

        final Voice voice = getDefaultVoice();

        if (voice != null) {
            final SaiyVoice saiyVoice = new SaiyVoice(voice);
            saiyVoice.setEngine(getInitialisedEngine());
            saiyVoice.setGender(saiyVoice.getName());

            if (DEBUG) {
                MyLog.i(CLS_NAME, "getEngineDefaultSaiyVoice: setting Gender: " + saiyVoice.getGender().name());
            }

            return saiyVoice;
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getEngineDefaultSaiyVoice: voice null");
            }
            return null;
        }
    }

    @Override
    public Voice getVoice() {
        return super.getVoice();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SaiyVoice getBoundSaiyVoice() {

        final Voice voice = getVoice();

        if (voice != null) {
            final SaiyVoice saiyVoice = new SaiyVoice(voice);
            saiyVoice.setEngine(getInitialisedEngine());
            saiyVoice.setGender(saiyVoice.getName());
            return saiyVoice;
        } else {
            return null;
        }
    }

    @Override
    public Set<Voice> getVoices() {
        final long then = System.nanoTime();

        if (defaultVoiceSet == null || defaultVoiceSet.isEmpty()) {
            defaultVoiceSet = super.getVoices();
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getVoices: already prepared");
            }
        }

        if (DEBUG) {
            MyLog.getElapsed("getVoices", then);
        }

        return defaultVoiceSet;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private Set<SaiyVoice> getSaiyVoices() {
        final long then = System.nanoTime();
        final Set<Voice> voiceSet = getVoices();

        if (saiyVoiceSet == null || saiyVoiceSet.size() != voiceSet.size()) {
            saiyVoiceSet = SaiyVoice.getSaiyVoices(voiceSet, getInitialisedEngine());
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getSaiyVoices: already prepared");
            }
        }

        if (DEBUG) {
            MyLog.getElapsed("getSaiyVoices", then);
        }
        return saiyVoiceSet;
    }

    @Override
    public int isLanguageAvailable(final Locale loc) {

        int result = LANG_NOT_SUPPORTED;

        try {
            result = super.isLanguageAvailable(loc);
        } catch (final IllegalArgumentException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "isLanguageAvailable: IllegalArgumentException: "
                        + loc.toString());
                e.printStackTrace();
            }
        }

        return result;
    }

    @Override
    public boolean isSpeaking() {

        if (audioTrack != null && audioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isSpeaking: audioTrack STATE_INITIALIZED");
            }

            if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING
                    || audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PAUSED) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "isSpeaking: audioTrack PLAYSTATE_PLAYING/PLAYSTATE_PAUSED");
                }
                return true;
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "isSpeaking: audioTrack not playing");
                }
            }
        }

        final boolean speakingSuper = super.isSpeaking();

        if (DEBUG) {
            MyLog.i(CLS_NAME, "isSpeaking: speakingSuper " + speakingSuper);
        }

        return speakingSuper;
    }

    @Override
    public int playEarcon(final String earcon, final int queueMode, final Bundle params, final String utteranceId) {
        return super.playEarcon(earcon, queueMode, params, utteranceId);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int playEarcon(final String earcon, final int queueMode, final HashMap<String, String> params) {
        return super.playEarcon(earcon, queueMode, params);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int playSilence(final long durationInMs, final int queueMode, final HashMap<String, String> params) {
        return super.playSilence(durationInMs, queueMode, params);
    }

    @Override
    public int playSilentUtterance(final long durationInMs, final int queueMode, final String utteranceId) {
        return super.playSilentUtterance(durationInMs, queueMode, utteranceId);
    }

    @Override
    public int setAudioAttributes(final AudioAttributes audioAttributes) {
        return super.setAudioAttributes(audioAttributes);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int setEngineByPackageName(final String enginePackageName) {
        return super.setEngineByPackageName(enginePackageName);
    }

    @Override
    public int setLanguage(final Locale loc) {
        return super.setLanguage(loc);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int setOnUtteranceCompletedListener(final OnUtteranceCompletedListener listener) {
        return super.setOnUtteranceCompletedListener(listener);
    }

    @Override
    public int setOnUtteranceProgressListener(final UtteranceProgressListener listener) {
        this.listener = (SaiyProgressListener) listener;
        return super.setOnUtteranceProgressListener(listener);
    }

    @Override
    public int setPitch(final float pitch) {
        return super.setPitch(pitch);
    }

    @Override
    public int setSpeechRate(final float speechRate) {
        return super.setSpeechRate(speechRate);
    }

    @Override
    public int setVoice(final Voice voice) {
        return super.setVoice(voice);
    }

    /**
     * Set the voice of the TTS object. API levels are handled here.
     *
     * @param language   the {@link Locale} language
     * @param region     the {@link Locale} region
     * @param conditions the {@link SelfAwareConditions}
     * @param params     the {@link SelfAwareParameters}
     * @return one of {@link #SUCCESS} or {@link #ERROR}
     */
    public int setVoice(@NonNull final String language, @NonNull final String region,
                        @NonNull final SelfAwareConditions conditions, @NonNull final SelfAwareParameters params) {

        int result;

        final String initialisedEngine = getInitialisedEngine();

        if (DEBUG) {
            MyLog.i(CLS_NAME, "setVoice: initialisedEngine: " + initialisedEngine);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && TTSDefaults.isApprovedVoice(getInitialisedEngine())) {

            result = setVoice21(language, region, conditions, params);

            if (DEBUG) {
                switch (result) {
                    case SUCCESS:
                        MyLog.i(CLS_NAME, "setVoice21: SUCCESS");
                        break;
                    case ERROR:
                        MyLog.w(CLS_NAME, "setVoice21: ERROR");
                        break;
                    default:
                        MyLog.w(CLS_NAME, "setVoice21: default");
                        break;
                }
            }

            if (result != SUCCESS) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "setVoice21: FAIL: notifying user");
                }
                conditions.showToast(mContext.getString(R.string.error_tts_voice), Toast.LENGTH_SHORT);
            }

            return result;
        } else {

            result = setVoiceDeprecated(language, region);

            if (DEBUG) {
                switch (result) {
                    case SUCCESS:
                        MyLog.i(CLS_NAME, "setVoiceDeprecated: SUCCESS");
                        break;
                    case ERROR:
                        MyLog.w(CLS_NAME, "setVoiceDeprecated: ERROR");
                        break;
                    default:
                        MyLog.w(CLS_NAME, "setVoiceDeprecated: default");
                        break;
                }
            }

            if (result != SUCCESS) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "setVoiceDeprecated: FAIL: notifying user");
                }
                conditions.showToast(mContext.getString(R.string.error_tts_voice), Toast.LENGTH_SHORT);
            }

            return result;
        }
    }

    /**
     * Automatically select the user's default voice.
     *
     * @param language   the {@link Locale} language
     * @param region     the {@link Locale} region
     * @param conditions the {@link SelfAwareConditions}
     * @param params     the {@link SelfAwareParameters}
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setDefaultVoice(@NonNull final String language, @NonNull final String region,
                                 @NonNull final SelfAwareConditions conditions,
                                 @NonNull final SelfAwareParameters params) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "setDefaultVoice");
        }

        final Pair<SaiyVoice, Locale> voicePair = new TTSVoice(language, region, conditions, params, null)
                .buildVoice();

        if (voicePair.first != null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "setDefaultVoice: Setting Voice: " + voicePair.first.toString());
            }

            final SaiyVoice saiyVoice = new SaiyVoice(voicePair.first);
            saiyVoice.setEngine(getInitialisedEngine());
            saiyVoice.setGender(saiyVoice.getName());

            final String gsonString = new GsonBuilder().disableHtmlEscaping().create().toJson(saiyVoice);

            if (DEBUG) {
                MyLog.i(CLS_NAME, "setDefaultVoice: gsonString: " + gsonString);
            }

            SPH.setDefaultTTSVoice(mContext, gsonString);

        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "setDefaultVoice: Unable to establish a default voice");
            }
        }
    }

    /**
     * Set the voice of the TTS object.
     *
     * @param language   the {@link Locale} language
     * @param region     the {@link Locale} region
     * @param conditions the {@link SelfAwareConditions}
     * @param params     the {@link SelfAwareParameters}
     * @return one of {@link #SUCCESS} or {@link #ERROR}
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private int setVoice21(@NonNull final String language, @NonNull final String region,
                           @NonNull final SelfAwareConditions conditions, @NonNull final SelfAwareParameters params) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "setVoice21");
        }

        final boolean isNetworkAvailable = params.shouldNetwork();

        SaiyVoice userDefaultSaiyVoice = null;

        try {

            switch (conditions.getCondition()) {

                case Condition.CONDITION_TRANSLATION:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "setVoice21: CONDITION_TRANSLATION");
                    }
                    break;
                default:

                    userDefaultSaiyVoice = getUserDefaultSaiyVoice();

                    if (userDefaultSaiyVoice == null) {
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                SaiyTextToSpeech.this.setDefaultVoice(language, region, conditions, params);
                            }
                        });
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "setVoice21: userDefaultSaiyVoice: " + userDefaultSaiyVoice.toString());
                        }

                        boolean exists = false;
                        for (final SaiyVoice voice : getSaiyVoices()) {
                            if (userDefaultSaiyVoice.getName().matches("(?i)" + Pattern.quote(voice.getName()))) {
                                exists = true;
                                break;
                            }
                        }

                        if (!exists) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "setVoice21: defaultSaiyVoice: no longer exists");
                            }
                            userDefaultSaiyVoice = null;
                            SPH.setDefaultTTSVoice(mContext, null);
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    SaiyTextToSpeech.this.setDefaultVoice(language, region, conditions, params);
                                }
                            });
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "setVoice21: defaultSaiyVoice: exists");
                            }
                        }
                    }

                    break;
            }

            SaiyVoice boundSaiyVoice = getBoundSaiyVoice();

            if (boundSaiyVoice == null) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "setVoice21: boundSaiyVoice null");
                }
                return setVoiceDeprecated(language, region);
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "setVoice21: boundSaiyVoice: " + boundSaiyVoice.toString());
                    MyLog.i(CLS_NAME, "setVoice21: boundSaiyVoice matches default: " + boundSaiyVoice.equals(userDefaultSaiyVoice));
                    MyLog.i(CLS_NAME, "setVoice21: boundSaiyVoice Locale: " + boundSaiyVoice.getLocale().toString());
                    MyLog.i(CLS_NAME, "setVoice21: Required Locale: " + language + " ~ " + region);
                }
            }

            switch (conditions.getCondition()) {

                case Condition.CONDITION_TRANSLATION:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "setVoice21: CONDITION_TRANSLATION");
                    }
                    break;
                default:

                    if (userDefaultSaiyVoice != null) {
                        if (!boundSaiyVoice.equals(userDefaultSaiyVoice)) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "setVoice21: userDefaultSaiyVoice engine: " + userDefaultSaiyVoice.getEngine());
                                MyLog.i(CLS_NAME, "setVoice21: boundSaiyVoice engine: " + boundSaiyVoice.getEngine());
                            }

                            if (userDefaultSaiyVoice.getEngine().matches(boundSaiyVoice.getEngine())) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "setVoice21: userDefaultSaiyVoice & boundSaiyVoice engines match");
                                }

                                if (isNetworkAvailable) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "setVoice21: engines match: network available: setting default");
                                    }
                                    setVoice(userDefaultSaiyVoice);
                                    boundSaiyVoice = getBoundSaiyVoice();
                                } else {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "setVoice21: engines match: no network");
                                    }

                                    if (!userDefaultSaiyVoice.isNetworkConnectionRequired()) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "setVoice21: engines match: userDefaultSaiyVoice network not needed");
                                        }
                                        setVoice(userDefaultSaiyVoice);
                                        boundSaiyVoice = getBoundSaiyVoice();
                                    } else {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "setVoice21: engines match: userDefaultSaiyVoice requires network");
                                        }

                                        final String utterance = conditions.getUtterance();

                                        if (UtilsString.notNaked(utterance) && !utterance.matches(SaiyRequestParams.SILENCE)) {

                                            if (synthesisAvailable(conditions.getUtterance(), userDefaultSaiyVoice.getName())) {
                                                if (DEBUG) {
                                                    MyLog.i(CLS_NAME, "setVoice21: synthesis cached: SUCCESS");
                                                }
                                                setVoice(userDefaultSaiyVoice);
                                                return SUCCESS;
                                            } else {
                                                if (DEBUG) {
                                                    MyLog.i(CLS_NAME, "setVoice21: no synthesis cache");
                                                }
                                            }
                                        } else {
                                            if (DEBUG) {
                                                MyLog.i(CLS_NAME, "setVoice21: engine warm up only");
                                            }
                                            return SUCCESS;
                                        }
                                    }
                                }
                            } else {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "setVoice21: userDefaultSaiyVoice & boundSaiyVoice engines don't match.");
                                }

                                saiyVoiceSet = null;
                            }
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "setVoice21: boundSaiyVoice and userDefaultSaiyVoice match");
                            }
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "setVoice21: defaultSaiyVoice: null");
                        }
                        SPH.setDefaultTTSVoice(mContext, null);
                    }

                    break;
            }

            if (!UtilsLocale.localesLanguageMatch(boundSaiyVoice.getLocale(), new Locale(language, region))) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "setVoice21: locales don't match");
                }
                return resolveVoice(language, region, conditions, params, boundSaiyVoice);
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "setVoice21: locales match");
                }

                if (boundSaiyVoice.isNetworkConnectionRequired()) {
                    if (isNetworkAvailable) {
                        if (SPH.getNetworkSynthesis(mContext)) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "setVoice21: network required: SUCCESS");
                            }
                            return SUCCESS;
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "setVoice21: user no network");
                            }
                            return resolveVoice(language, region, conditions, params, boundSaiyVoice);
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "setVoice21: network unavailable");
                        }

                        final String utterance = conditions.getUtterance();

                        if (UtilsString.notNaked(utterance) && !utterance.matches(SaiyRequestParams.SILENCE)) {

                            if (synthesisAvailable(conditions.getUtterance(), boundSaiyVoice.getName())) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "setVoice21: synthesis cached: SUCCESS");
                                }
                                return SUCCESS;
                            } else {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "setVoice21: no synthesis cache");
                                }
                                return resolveVoice(language, region, conditions, params, boundSaiyVoice);
                            }
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "setVoice21: engine warm up only");
                            }
                            return SUCCESS;
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "setVoice21: no network required: SUCCESS");
                    }
                    return SUCCESS;
                }
            }
        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "setVoice21 NullPointerException");
                e.printStackTrace();
            }
        } catch (final MissingResourceException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "setVoice21 MissingResourceException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "setVoice21 Exception");
                e.printStackTrace();
            }
            if (e instanceof IllformedLocaleException) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "setVoice21 IllformedLocaleException");
                }
            }
        }

        if (DEBUG) {
            MyLog.i(CLS_NAME, "setVoice21: falling back to setVoiceDeprecated");
        }

        return setVoiceDeprecated(language, region);
    }

    /**
     * Attempt to resolve the voice that most suits the conditions and the user's preferences.
     *
     * @param language   the {@link Locale} language
     * @param region     the {@link Locale} region
     * @param conditions the {@link SelfAwareConditions}
     * @param params     the {@link SelfAwareParameters}
     * @return one of {@link #SUCCESS} or {@link #ERROR}
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private int resolveVoice(@NonNull final String language, @NonNull final String region,
                             @NonNull final SelfAwareConditions conditions, @NonNull final SelfAwareParameters params,
                             @Nullable final SaiyVoice currentVoice) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "resolveVoice");
        }

        final Pair<SaiyVoice, Locale> voicePair = new TTSVoice(language, region, conditions, params,
                currentVoice).buildVoice();

        if (voicePair.first != null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "resolveVoice: Setting Voice: " + voicePair.first.toString());
                MyLog.i(CLS_NAME, "resolveVoice: Setting Voice loc: " + voicePair.first.getLocale());
                try {
                    MyLog.i(CLS_NAME, "resolveVoice: Setting Voice: isLanguageAvailable: "
                            + resolveSuccess(isLanguageAvailable(new Locale(voicePair.first.getLocale().getLanguage(),
                            voicePair.first.getLocale().getCountry()))));
                } catch (final MissingResourceException e) {
                    MyLog.w(CLS_NAME, "MissingResourceException: isLanguageAvailable failed");
                    e.printStackTrace();
                }
            }

            return super.setVoice(voicePair.first);
        } else {
            if (voicePair.second != null) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "resolveVoice: Setting Locale deprecated");
                }
                return setVoiceDeprecated(voicePair.second.getLanguage(), voicePair.second.getCountry());
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "resolveVoice: voicePair.second null: falling back");
                }
                return resolveSuccess(super.setLanguage(new Locale(language, region)));
            }
        }
    }

    /**
     * Set the voice/language of the TTS object for lower API levels, or if the current engine does
     * not support the latest APIs.
     *
     * @param language the {@link Locale} language
     * @param region   the {@link Locale} region
     * @return one of {@link #SUCCESS} or {@link #ERROR}
     */

    private int setVoiceDeprecated(@NonNull final String language, @NonNull final String region) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "setVoiceDeprecated: Setting Locale");
        }

        try {

            Locale locale = new Locale(language, region);

            if (DEBUG) {
                MyLog.i(CLS_NAME, "setVoiceDeprecated: comparing current: " + getLanguage()
                        + " with " + locale.toString());
            }

            if (!UtilsLocale.localesMatch(getLanguage(), locale)) {

                try {
                    locale.getCountry();
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "setVoiceDeprecated: isLanguageAvailable: "
                                + resolveSuccess(super.isLanguageAvailable(new Locale(locale.getLanguage(),
                                locale.getCountry()))));
                    }
                } catch (final MissingResourceException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "MissingResourceException: Just using language");
                        e.printStackTrace();
                    }
                    locale = new Locale(locale.getLanguage());
                }

                return resolveSuccess(super.setLanguage(locale));
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "setVoice: Current matches");
                }
                return SUCCESS;
            }
        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "NullPointerException");
                e.printStackTrace();
            }
        } catch (final MissingResourceException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "MissingResourceException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "Exception");
                e.printStackTrace();
            }
        }

        return ERROR;
    }


    @Override
    public void shutdown() {

        if (audioTrack != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            switch (audioTrack.getPlayState()) {
                case AudioTrack.PLAYSTATE_PLAYING:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "stop: PLAYSTATE_PLAYING");
                    }
                    audioTrack.stop(true);
                    audioTrack.flush();
                    audioTrack.release();
                case AudioTrack.PLAYSTATE_PAUSED:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "stop: PLAYSTATE_PAUSED");
                    }
                    audioTrack.stop(true);
                    audioTrack.flush();
                    audioTrack.release();
                case AudioTrack.PLAYSTATE_STOPPED:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "stop: PLAYSTATE_STOPPED");
                    }
                    audioTrack.flush();
                    audioTrack.release();
                    break;
            }
        }

        super.shutdown();
    }

    @Override
    public int stop() {

        if (audioTrack != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            switch (audioTrack.getPlayState()) {
                case AudioTrack.PLAYSTATE_PLAYING:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "stop: PLAYSTATE_PLAYING");
                    }
                    audioTrack.stop(true);
                    return SUCCESS;
                case AudioTrack.PLAYSTATE_PAUSED:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "stop: PLAYSTATE_PAUSED");
                    }
                    audioTrack.stop(true);
                    return SUCCESS;
                case AudioTrack.PLAYSTATE_STOPPED:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "stop: PLAYSTATE_STOPPED");
                    }
                    break;
            }
        }

        try {
            return super.stop();
        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "stop: NullPointerException");
                e.printStackTrace();
            }
        }

        return ERROR;
    }

    @Override
    public int synthesizeToFile(final CharSequence text, final Bundle params, final File file, final String utteranceId) {
        return super.synthesizeToFile(text, params, file, utteranceId);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int synthesizeToFile(final String text, final HashMap<String, String> params, final String filename) {
        return super.synthesizeToFile(text, params, filename);
    }

    /**
     * Get the previously initialised TTS Engine package name. If it doesn't match the user's default
     * we need to reinitialise the Engine.
     *
     * @return the package name of the currently initialised TTS Engine.
     */
    public String getInitialisedEngine() {
        if (initEngine == null) {
            return "";
        } else {
            return this.initEngine;
        }
    }

    /**
     * Store the initialised engine package name, so to check in the future if the user has changed
     * their default TTS Engine in the Android Settings. There is no Broadcast exposed to handle
     * this elsewhere, so we need to use reflection to access the variable in the super class.
     *
     * @param initEngine the user's default Text to Speech Engine in the Android Application Settings
     */
    private void setInitialisedEngine(@NonNull final String initEngine) {

        String reflectEngine;

        try {
            final Method method = this.getClass().getSuperclass().getMethod(TTSDefaults.BOUND_ENGINE_METHOD);
            reflectEngine = (String) method.invoke(this);

            if (UtilsString.notNaked(reflectEngine)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Method reflect: reflectEngine: " + reflectEngine);
                }

                this.initEngine = reflectEngine;
                return;
            }
        } catch (final NoSuchMethodException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "NoSuchMethodException");
                e.printStackTrace();
            }
        } catch (final IllegalAccessException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "IllegalAccessException");
                e.printStackTrace();
            }
        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "NullPointerException");
                e.printStackTrace();
            }
        } catch (InvocationTargetException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "InvocationTargetException");
                e.printStackTrace();
            }
        }

        try {
            final Field f = this.getClass().getSuperclass().getDeclaredField(TTSDefaults.BOUND_ENGINE_FIELD);
            f.setAccessible(true);
            reflectEngine = (String) f.get(this);

            if (UtilsString.notNaked(reflectEngine)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Field reflect: reflectEngine: " + reflectEngine);
                }

                this.initEngine = reflectEngine;
                return;
            }

        } catch (final NoSuchFieldException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "NoSuchFieldException");
                e.printStackTrace();
            }
        } catch (final IllegalAccessException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "IllegalAccessException");
                e.printStackTrace();
            }
        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "NullPointerException");
                e.printStackTrace();
            }
        }

        if (UtilsString.notNaked(initEngine)) {
            this.initEngine = initEngine;
        } else {
            this.initEngine = "";
        }

        if (DEBUG) {
            MyLog.i(CLS_NAME, "initEngine: " + this.initEngine);
        }
    }

    /**
     * Store the initialised Text to Speech Engine
     */
    public void initialised() {

        try {

            final String packageName = getDefaultEngineSecure();
            setInitialisedEngine(packageName);

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

        if (DEBUG) {
            //getInfo();
        }

        addSoundEffects();
    }


    /**
     * Examine TTS objects in an overly verbose way. Debugging only.
     */
    @SuppressWarnings("deprecation")
    private void getInfo() {
        MyLog.i(CLS_NAME, "getQuickInfo");

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                    try {
                        MyLog.v(CLS_NAME, "getEngineDefaultSaiyVoice: " + SaiyTextToSpeech.this.getEngineDefaultSaiyVoice().toString());
                        MyLog.v(CLS_NAME, "getBoundSaiyVoice: " + SaiyTextToSpeech.this.getBoundSaiyVoice().toString());

                        final SaiyVoice userDefaultSaiyVoice = SaiyTextToSpeech.this.getUserDefaultSaiyVoice();

                        if (userDefaultSaiyVoice != null) {
                            MyLog.v(CLS_NAME, "userDefaultSaiyVoice: " + userDefaultSaiyVoice.toString());
                        }

                    } catch (final NullPointerException e) {
                        MyLog.w(CLS_NAME, "NullPointerException");
                        e.printStackTrace();
                    } catch (final Exception e) {
                        MyLog.w(CLS_NAME, "Exception");
                        e.printStackTrace();
                    } finally {

                        try {
                            final Locale defaultLanguage = SaiyTextToSpeech.this.getDefaultLanguage();
                            MyLog.v(CLS_NAME, "defaultLanguage toString: " + defaultLanguage.toString());
                        } catch (final NullPointerException e) {
                            MyLog.w(CLS_NAME, "NullPointerException");
                            e.printStackTrace();
                        } catch (final Exception e) {
                            MyLog.w(CLS_NAME, "Exception");
                            e.printStackTrace();
                        } finally {
                            try {
                                final Locale languageLocale = SaiyTextToSpeech.this.getLanguage();
                                MyLog.v(CLS_NAME, "languageLocale toString: " + languageLocale.toString());
                            } catch (final NullPointerException e) {
                                MyLog.w(CLS_NAME, "NullPointerException");
                                e.printStackTrace();
                            } catch (final Exception e) {
                                MyLog.w(CLS_NAME, "Exception");
                                e.printStackTrace();
                            }
                        }
                    }

                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {

                    try {
                        final Locale defaultLanguage = SaiyTextToSpeech.this.getDefaultLanguage();
                        MyLog.v(CLS_NAME, "defaultLanguage toString: " + defaultLanguage.toString());
                    } catch (final NullPointerException e) {
                        MyLog.w(CLS_NAME, "NullPointerException");
                        e.printStackTrace();
                    } catch (final Exception e) {
                        MyLog.w(CLS_NAME, "Exception");
                        e.printStackTrace();
                    } finally {
                        try {
                            final Locale languageLocale = SaiyTextToSpeech.this.getLanguage();
                            MyLog.v(CLS_NAME, "languageLocale toString: " + languageLocale.toString());
                        } catch (final NullPointerException e) {
                            MyLog.w(CLS_NAME, "NullPointerException");
                            e.printStackTrace();
                        } catch (final Exception e) {
                            MyLog.w(CLS_NAME, "Exception");
                            e.printStackTrace();
                        }
                    }
                } else {
                    try {
                        final Locale languageLocale = SaiyTextToSpeech.this.getLanguage();
                        MyLog.v(CLS_NAME, "languageLocale toString: " + languageLocale.toString());
                    } catch (final NullPointerException e) {
                        MyLog.w(CLS_NAME, "NullPointerException");
                        e.printStackTrace();
                    } catch (final Exception e) {
                        MyLog.w(CLS_NAME, "Exception");
                        e.printStackTrace();
                    }
                }

                SaiyTextToSpeech.this.getVerboseInfo();
            }
        });
    }

    /**
     * More debugging info
     */
    private void getVerboseInfo() {
        MyLog.i(CLS_NAME, "getVerboseInfo");

        try {
            final List<EngineInfo> engines = getEngines();

            for (int i = 0; i < engines.size(); i++) {
                MyLog.v(CLS_NAME, "inf label: " + engines.get(i).label);
                MyLog.v(CLS_NAME, "inf name: " + engines.get(i).name);
            }

        } catch (final NullPointerException e) {
            MyLog.w(CLS_NAME, "NullPointerException");
            e.printStackTrace();
        } catch (final Exception e) {
            MyLog.w(CLS_NAME, "Exception");
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            try {

                for (final SaiyVoice v : getSaiyVoices()) {
                    MyLog.v(CLS_NAME, "v : " + v.toString());
                }

            } catch (final NullPointerException e) {
                MyLog.w(CLS_NAME, "NullPointerException");
                e.printStackTrace();
            } catch (final Exception e) {
                MyLog.w(CLS_NAME, "Exception");
                e.printStackTrace();
            }

            try {

                for (final Locale loc : getAvailableLanguages()) {
                    MyLog.v(CLS_NAME, "loc: " + loc.toString());
                }

            } catch (final NullPointerException e) {
                MyLog.w(CLS_NAME, "NullPointerException");
                e.printStackTrace();
            } catch (final Exception e) {
                MyLog.w(CLS_NAME, "Exception");
                e.printStackTrace();
            }
        }
    }

    /**
     * Convert the responses of {@link #LANG_COUNTRY_AVAILABLE} {@link #LANG_AVAILABLE}
     * {@link #LANG_COUNTRY_VAR_AVAILABLE} {@link #LANG_MISSING_DATA}
     * {@link #LANG_NOT_SUPPORTED} to {@link #SUCCESS} {@link #ERROR}
     *
     * @param result the result of attempting to set a voice/language {@link Locale}
     * @return one of {@link #SUCCESS} {@link #ERROR}
     */
    private int resolveSuccess(final int result) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "resolveSuccess");
        }

        switch (result) {

            case LANG_AVAILABLE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "resolveSuccess: LANG_AVAILABLE");
                }
                return SUCCESS;
            case LANG_COUNTRY_AVAILABLE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "resolveSuccess: LANG_COUNTRY_AVAILABLE");
                }
                return SUCCESS;
            case LANG_COUNTRY_VAR_AVAILABLE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "resolveSuccess: LANG_COUNTRY_VAR_AVAILABLE");
                }
                return SUCCESS;
            case LANG_MISSING_DATA:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "resolveSuccess: LANG_MISSING_DATA");
                }
                return ERROR;
            case LANG_NOT_SUPPORTED:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "resolveSuccess: LANG_NOT_SUPPORTED");
                }
                return ERROR;
            default:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "resolveSuccess: default");
                }
                return ERROR;
        }
    }

    /**
     * Attempt to select a voice object based on the user's preference or required locale. The
     * omission of parameters that let us know if the engine is correctly installed is a major
     * frustration here, along with no providers distinguishing between the gender of their voices.
     * <p/>
     * I could rant further....
     * <p/>
     * Created by benrandall76@gmail.com on 13/03/2016.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private class TTSVoice {

        private final boolean DEBUG = MyLog.DEBUG;
        private final String CLS_NAME = TTSVoice.class.getSimpleName();

        private final String language;
        private final String region;
        private final SelfAwareConditions conditions;
        private final SelfAwareParameters params;
        private final boolean isNetworkAllowed;
        private final boolean isNetworkAvailable;
        private final SaiyVoice currentVoice;

        /**
         * Constructor
         *
         * @param language the {@link Locale} language
         * @param region   the {@link Locale} region
         */
        private TTSVoice(@NonNull final String language, @NonNull final String region,
                         @NonNull final SelfAwareConditions conditions,
                         @NonNull final SelfAwareParameters params, @Nullable final SaiyVoice currentVoice) {
            this.language = language;
            this.region = region;
            this.conditions = conditions;
            this.params = params;
            this.currentVoice = currentVoice;

            isNetworkAllowed = this.params.isNetworkAllowed();
            isNetworkAvailable = this.params.shouldNetwork();

        }

        private Pair<SaiyVoice, Locale> buildVoice() {

            Locale requiredLocale = null;
            SaiyVoice voice = null;

            try {

                final Set<SaiyVoice> voices = getSaiyVoices();
                requiredLocale = new Locale(language, region);

                if (conditions.getCondition() != Condition.CONDITION_TRANSLATION) {

                    if (currentVoice != null && getInitialisedEngine().startsWith(TTSDefaults.TTS_PKG_NAME_GOOGLE)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "Have a current google voice");
                        }

                        final TTSDefaults.Google parentGoogle = TTSDefaults.Google.getGoogle(currentVoice.getName());

                        if (parentGoogle != null) {

                            SaiyVoice associatedVoice = null;
                            TTSDefaults.Google associatedGoogle = null;

                            for (final SaiyVoice v : voices) {
                                if (parentGoogle.getVoiceName().matches("(?i)" + Pattern.quote(v.getName()))) {

                                    if (v.isNetworkConnectionRequired()) {
                                        associatedGoogle = TTSDefaults.Google.getAssociatedVoice(parentGoogle,
                                                TTSDefaults.TYPE_LOCAL);
                                    } else {
                                        associatedGoogle = TTSDefaults.Google.getAssociatedVoice(parentGoogle,
                                                TTSDefaults.TYPE_NETWORK);
                                    }

                                    break;
                                }
                            }

                            if (associatedGoogle != null) {
                                for (final SaiyVoice v : voices) {
                                    if (associatedGoogle.getVoiceName().matches("(?i)" + Pattern.quote(v.getName()))) {
                                        associatedVoice = v;
                                    }
                                }

                                if (associatedVoice != null) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "returning associated: " + associatedVoice.getName());
                                    }
                                    return new Pair<>(associatedVoice, requiredLocale);
                                }
                            } else {
                                if (DEBUG) {
                                    MyLog.w(CLS_NAME, "associated google null");
                                }
                            }
                        } else {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "parent google null");
                            }
                        }
                    }

                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "buildVoice: CONDITION_TRANSLATION");
                    }

                    final ArrayList<SaiyVoice> voiceArray = new ArrayList<>();
                    final ArrayList<SaiyVoice> voiceExactArray = new ArrayList<>();

                    if (!voices.isEmpty()) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "Have " + voices.size() + " starting voices");
                        }

                        for (final SaiyVoice v : voices) {
                            if (UtilsLocale.localesMatch(v.getLocale(), requiredLocale)) {
                                if (DEBUG) {
                                    MyLog.v(CLS_NAME, "v : " + v.toString());
                                }
                                voiceArray.add(v);
                            }
                        }

                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "Have " + voiceArray.size() + " potential voices");
                        }

                        if (voiceArray.isEmpty()) {
                            if (DEBUG) {
                                MyLog.v(CLS_NAME, "Checking ISO ");
                            }

                            for (final SaiyVoice v : voices) {
                                if (v.getLocale().getISO3Language().matches(requiredLocale.getISO3Language())) {
                                    if (DEBUG) {
                                        MyLog.v(CLS_NAME, "vISO language: " + v.getLocale().getISO3Language());
                                    }
                                    if (v.getLocale().getISO3Country().matches(requiredLocale.getISO3Country())) {
                                        if (DEBUG) {
                                            MyLog.v(CLS_NAME, "vISO country: " + v.getLocale().getISO3Country());
                                        }
                                        voiceArray.add(0, v);
                                    } else {
                                        voiceArray.add(v);
                                    }
                                }
                            }
                        }

                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "Have " + voiceArray.size() + " potential voices");
                        }

                        if (voiceArray.isEmpty()) {
                            if (DEBUG) {
                                MyLog.v(CLS_NAME, "Checking loose ISO ");
                            }

                            for (final SaiyVoice v : voices) {
                                if (v.getLocale().getISO3Language().matches(requiredLocale.getLanguage())) {
                                    if (DEBUG) {
                                        MyLog.v(CLS_NAME, "vISO language: " + v.getLocale().getLanguage());
                                    }
                                    if (v.getLocale().getISO3Country().matches(requiredLocale.getCountry())) {
                                        if (DEBUG) {
                                            MyLog.v(CLS_NAME, "vISO country: " + v.getLocale().getCountry());
                                        }
                                        voiceArray.add(0, v);
                                    } else {
                                        voiceArray.add(v);
                                    }
                                }
                            }
                        }

                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "Have " + voiceArray.size() + " potential voices");
                        }

                        if (!voiceArray.isEmpty()) {

                            if (!isNetworkAllowed || !isNetworkAvailable) {
                                if (DEBUG) {
                                    MyLog.v(CLS_NAME, "Removing networked voices");
                                }

                                final ListIterator<SaiyVoice> itr = voiceArray.listIterator();

                                SaiyVoice v;
                                while (itr.hasNext()) {
                                    v = itr.next();
                                    if ((v.isNetworkConnectionRequired()
                                            && !v.getFeatures().contains(TTSDefaults.EMBEDDED_TTS_FIELD))
                                            || v.getFeatures().contains(Engine.KEY_FEATURE_NOT_INSTALLED)) {
                                        if (DEBUG) {
                                            MyLog.v(CLS_NAME, "Removing networked voice: " + v.toString());
                                        }
                                        itr.remove();
                                    }
                                }
                            }

                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "Have " + voiceArray.size() + " potential voices");
                            }

                            if (!voiceArray.isEmpty()) {

                                for (final SaiyVoice v : voiceArray) {
                                    if (isNetworkAllowed) {
                                        if (v.isNetworkConnectionRequired()) {
                                            voiceExactArray.add(0, v);
                                        }
                                    } else {
                                        voiceExactArray.add(v);
                                    }
                                }

                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "Have " + voiceExactArray.size() + " exact voices");
                                }

                                if (!voiceExactArray.isEmpty()) {
                                    if (DEBUG) {
                                        for (final SaiyVoice v : voiceExactArray) {
                                            MyLog.v(CLS_NAME, "Exact Voice " + v.toString());
                                        }
                                    }

                                    voice = getVoiceDetailed(voiceExactArray);

                                } else {
                                    if (DEBUG) {
                                        for (final SaiyVoice v : voiceArray) {
                                            MyLog.v(CLS_NAME, "Settled Voice " + v.toString());
                                        }
                                    }

                                    voice = getVoiceDetailed(voiceArray);
                                }
                            } else {
                                if (DEBUG) {
                                    MyLog.w(CLS_NAME, "Only networked voices available");
                                }
                            }
                        } else {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "Could not match engine Locale");
                            }
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "Engine has no voices?");
                        }
                    }
                }
            } catch (final MissingResourceException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "MissingResourceException");
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
                if (e instanceof IllformedLocaleException) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "IllformedLocaleException");
                    }
                }
            }

            if (voice != null) {
                voice.setEngine(getInitialisedEngine());
                voice.setGender(voice.getName());
                return new Pair<>(voice, requiredLocale);
            } else {
                return new Pair<>(null, requiredLocale);
            }
        }

        private SaiyVoice getVoiceDetailed(@NonNull final ArrayList<SaiyVoice> voiceArray) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getVoiceDetailed");
            }

            if (voiceArray.size() > 1) {
                return filterGender(voiceArray).get(0);
            } else {
                return voiceArray.get(0);
            }
        }

        private ArrayList<SaiyVoice> filterGender(@NonNull final ArrayList<SaiyVoice> voiceArray) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "filterGender");
            }

            final Gender preferredGender = SPH.getDefaultTTSGender(mContext);
            final ArrayList<SaiyVoice> voiceArrayCopy = new ArrayList<>(voiceArray);

            final ListIterator<SaiyVoice> itr = voiceArrayCopy.listIterator();

            SaiyVoice v;
            while (itr.hasNext()) {
                v = itr.next();
                if (v.getGender() != preferredGender) {
                    itr.remove();
                }
            }

            if (voiceArrayCopy.isEmpty()) {
                return filterLegacy(voiceArray);
            } else {
                return filterLegacy(voiceArrayCopy);
            }
        }

        private ArrayList<SaiyVoice> filterLegacy(@NonNull final ArrayList<SaiyVoice> voiceArray) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "filterLegacy");
            }

            if (isNetworkAllowed) {

                final ArrayList<SaiyVoice> voiceArrayCopy = new ArrayList<>(voiceArray);
                final ListIterator<SaiyVoice> itr = voiceArrayCopy.listIterator();

                SaiyVoice v;
                while (itr.hasNext()) {
                    v = itr.next();
                    if (v.getFeatures().contains(TTSDefaults.LEGACY_ENGINE_FIELD)) {
                        itr.remove();
                    }
                }

                if (voiceArrayCopy.isEmpty()) {
                    return filterQuality(voiceArray);
                } else {
                    return filterQuality(voiceArrayCopy);
                }
            }

            return filterQuality(voiceArray);
        }


        private ArrayList<SaiyVoice> filterQuality(@NonNull final ArrayList<SaiyVoice> voiceArray) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "filterQuality");
            }

            Collections.sort(voiceArray, new Comparator<SaiyVoice>() {
                @Override
                public int compare(final SaiyVoice v1, final SaiyVoice v2) {
                    return v2.getQuality() - v1.getQuality();
                }
            });
            return voiceArray;
        }
    }
}
