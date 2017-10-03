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

package ai.saiy.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Locale;

import ai.saiy.android.algorithms.Algorithm;
import ai.saiy.android.algorithms.distance.jarowinkler.JaroWinklerHelper;
import ai.saiy.android.algorithms.distance.levenshtein.LevenshteinHelper;
import ai.saiy.android.algorithms.fuzzy.FuzzyHelper;
import ai.saiy.android.algorithms.mongeelkan.MongeElkanHelper;
import ai.saiy.android.algorithms.needlemanwunch.NeedlemanWunschHelper;
import ai.saiy.android.algorithms.soundex.SoundexHelper;
import ai.saiy.android.api.SaiyDefaults;
import ai.saiy.android.api.request.SaiyRequestParams;
import ai.saiy.android.applications.Installed;
import ai.saiy.android.cognitive.emotion.provider.beyondverbal.containers.BVCredentials;
import ai.saiy.android.cognitive.motion.provider.google.Motion;
import ai.saiy.android.command.battery.BatteryInformation;
import ai.saiy.android.command.translate.provider.TranslationProvider;
import ai.saiy.android.command.unknown.Unknown;
import ai.saiy.android.database.DBSpeech;
import ai.saiy.android.defaults.songrecognition.SongRecognitionProvider;
import ai.saiy.android.memory.Memory;
import ai.saiy.android.recognition.provider.android.RecognitionNative;
import ai.saiy.android.service.SelfAware;
import ai.saiy.android.service.helper.SelfAwareConditions;
import ai.saiy.android.tts.attributes.Gender;
import ai.saiy.android.utils.Conditions.Network;

/**
 * Created by benrandall76@gmail.com on 07/02/2016.
 * <p/>
 * A helper class (Shared Preference Helper (SPH)) to access the user's preferences and retain
 * certain interaction information.
 * <p/>
 * The static nature of these global user variables does not appear to be a performance issue
 * and provides ease of access.
 */
public class SPH {

    private static final String SAIY_PREF = "saiyPref";

    private static final int ZERO = 0;
    private static final int ONE = 1;

    private static final String DEFAULT_RECOGNITION = "default_recognition";
    private static final String DEFAULT_LANGUAGE_MODEL = "default_language_model";
    private static final String DEFAULT_TTS = "default_tts";
    private static final String DEFAULT_TTS_GENDER = "default_tts_gender";
    private static final String DEFAULT_TEMPERATURE_UNITS = "default_temperature_units";
    private static final String DEFAULT_RINGER = "default_ringer";
    private static final String INACTIVITY_TIMEOUT = "inactivity_timeout";
    private static final String VR_LOCALE = "vr_locale";
    private static final String TTS_LOCALE = "tts_locale";
    private static final String TTS_VOLUME = "tts_volume";
    private static final String CUSTOM_INTRO = "custom_intro";
    private static final String CUSTOM_INTRO_RANDOM = "custom_intro_random";
    private static final String DEFAULT_TTS_VOICE = "default_tts_voice";
    private static final String SPELL_COMMAND_VERBOSE = "spell_command_verbose";
    private static final String EMOTION_COMMAND_VERBOSE = "emotion_command_verbose";
    private static final String TRANSLATE_COMMAND_VERBOSE = "translate_command_verbose";
    private static final String EMOTION_PERMISSION = "emotion_permission";
    private static final String VIBRATE = "vibrate";
    private static final String NETWORK_SYNTHESIS = "network_synthesis";
    private static final String USE_OFFLINE = "use_offline";
    private static final String PING_CHECK = "ping_check";
    private static final String COMMAND_UNKNOWN_ACTION = "command_unknown_action";
    private static final String INTERCEPT_GOOGLE_NOW = "intercept_google_now";
    private static final String PING_TIMEOUT = "ping_timeout";
    private static final String CONNECTION_MINIMUM = "connection_minimum";
    private static final String USER_NAME = "user_name";
    private static final String HOTWORD = "hotword";
    private static final String HOTWORD_BOOT = "hotword_boot";
    private static final String HOTWORD_DRIVING = "hotword_driving";
    private static final String HOTWORD_WAKELOCK = "hotword_wakelock";
    private static final String HOTWORD_SECURE = "hotword_secure";
    private static final String BOOT_START = "boot_start";
    private static final String SELF_AWARE_ENABLED = "self_aware_enabled";
    private static final String ENROLLMENT_VERBOSE = "enrollment_verbose";
    private static final String DISCLAIMER = "disclaimer";
    private static final String WHATS_NEW = "whats_new";
    private static final String DEVELOPER_NOTE = "developer_note";
    private static final String PAUSE_TIMEOUT = "pause_timeout";
    private static final String BING_TOKEN = "bing_token";
    private static final String BEYOND_VERBAL_AUTH_RESPONSE = "beyond_verbal_auth_response";
    private static final String BING_OAUTH_UPDATE = "bing_oauth_update";
    private static final String TRANSLATION_PROVIDER = "translation_provider";
    private static final String SAIY_ACCOUNTS = "saiy_accounts";
    private static final String MEMORY = "memory";
    private static final String EMOTION = "emotion";
    private static final String MOTION = "motion";
    private static final String MOTION_ENABLED = "motion_enabled";
    private static final String TOAST_UNKNOWN = "toast_unknown";
    private static final String BLACKLIST = "blacklist";
    private static final String ALGORITHM = "algorithm";
    private static final String ALGORITHMS = "algorithms";
    private static final String JWD_LOWER_THRESHOLD = "jwd_lower_threshold";
    private static final String JWD_UPPER_THRESHOLD = "jwd_upper_threshold";
    private static final String LEV_UPPER_THRESHOLD = "lev_upper_threshold";
    private static final String ME_UPPER_THRESHOLD = "me_upper_threshold";
    private static final String FUZZY_MULTIPLIER = "fuzzy_multiplier";
    private static final String NW_UPPER_THRESHOLD = "nw_upper_threshold";
    private static final String SOUNDEX_UPPER_THRESHOLD = "soundex_upper_threshold";
    private static final String REMOTE_COMMAND_VERBOSE = "remote_command_verbose";
    private static final String LAST_USED = "last_used";
    private static final String USED_INCREMENT = "used_increment";
    private static final String MAX_SPEECH_CACHE_SIZE = "max_speech_cache_size";
    private static final String DEFAULT_SONG_RECOGNITION = "default_song_recognition";
    private static final String ANNOUNCE_TASKER = "announce_tasker";
    private static final String ANNOUNCE_NOTIFICATIONS = "announce_notifications";
    private static final String RECOGNISER_BUSY_FIX = "recogniser_busy_fix";
    private static final String OKAY_GOOGLE_FIX = "okay_google_fix";

    /**
     * Prevent instantiation
     */
    public SPH() {
        throw new IllegalArgumentException(Resources.getSystem().getString(android.R.string.no));
    }

    /**
     * For convenience
     *
     * @param ctx the application context
     * @return the {@link SharedPreferences} object
     */
    private static SharedPreferences getPref(@NonNull final Context ctx) {
        return ctx.getSharedPreferences(SAIY_PREF, Context.MODE_PRIVATE);
    }

    /**
     * For convenience
     *
     * @param pref {@link SharedPreferences} object
     * @return the {@link android.content.SharedPreferences.Editor} object
     */
    private static SharedPreferences.Editor getEditor(final SharedPreferences pref) {
        return pref.edit();
    }

    ////////////////////////////////////////////////////////////////////////////////
    //                           START OF METHODS                                 //
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * Get the default recognition provider
     *
     * @param ctx the application context
     * @return the default recognition provider
     */
    public static SaiyDefaults.VR getDefaultRecognition(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return SaiyDefaults.getProviderVR(pref.getString(DEFAULT_RECOGNITION, SaiyDefaults.VR.NATIVE.name()));
    }

    /**
     * Set the default recognition provider
     *
     * @param ctx      the application context
     * @param provider of the recognition
     */
    public static void setDefaultRecognition(@NonNull final Context ctx, final SaiyDefaults.VR provider) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(DEFAULT_RECOGNITION, provider.name());
        edit.commit();
    }

    /**
     * Get the default Language Model
     *
     * @param ctx the application context
     * @return the default language model
     */
    public static SaiyDefaults.LanguageModel getDefaultLanguageModel(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return SaiyDefaults.getLanguageModel(pref.getString(DEFAULT_LANGUAGE_MODEL,
                SaiyDefaults.LanguageModel.LOCAL.name()));
    }

    /**
     * Set the default Language Model
     *
     * @param ctx   the application context
     * @param model to apply
     */
    public static void setDefaultLanguageModel(@NonNull final Context ctx, final SaiyDefaults.LanguageModel model) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(DEFAULT_LANGUAGE_MODEL, model.name());
        edit.commit();
    }

    /**
     * Get the amount of times the user has been informed of this verbose information
     *
     * @param ctx the application context
     * @return the integer number of times
     */
    public static int getEmotionCommandVerbose(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getInt(EMOTION_COMMAND_VERBOSE, ZERO);
    }

    /**
     * Increment the number of times the user has been informed of this verbose information
     *
     * @param ctx the application context
     */
    public static void incrementEmotionCommandVerbose(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putInt(EMOTION_COMMAND_VERBOSE, (getEmotionCommandVerbose(ctx) + 1));
        edit.commit();
    }

    /**
     * Set whether the Saiy should announce the name of the executed Tasker Task
     *
     * @param ctx       the application context
     * @param condition to apply
     */
    public static void setAnnounceTasker(@NonNull final Context ctx, final boolean condition) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(ANNOUNCE_TASKER, condition);
        edit.commit();
    }

    /**
     * Get whether the Saiy should announce the name of the executed Tasker Task
     *
     * @param ctx the application context
     * @return true as the default, or false if the user has disabled this
     */
    public static boolean getAnnounceTasker(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(ANNOUNCE_TASKER, true);
    }

    /**
     * Get the user preferred Text to Speech volume level
     *
     * @param ctx the application context
     * @return the user preferred volume level
     */
    public static int getTTSVolume(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getInt(TTS_VOLUME, ZERO);
    }

    /**
     * Set the user preferred Text to Speech volume level
     *
     * @param ctx   the application context
     * @param level of the volume
     */
    public static void setTTSVolume(@NonNull final Context ctx, final int level) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putInt(TTS_VOLUME, level);
        edit.commit();
    }

    /**
     * Get the default ringer configuration
     *
     * @param ctx the application context
     * @return the default recognition provider
     */
    public static int getDefaultRinger(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getInt(DEFAULT_RINGER, 99);
    }

    /**
     * Set the default ringer configuration
     *
     * @param ctx           the application context
     * @param ringerDefault of the recognition
     */
    public static void setDefaultRinger(@NonNull final Context ctx, final int ringerDefault) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putInt(DEFAULT_RINGER, ringerDefault);
        edit.commit();
    }

    /**
     * Get the user preferred temperature units
     *
     * @param ctx the application context
     * @return the default units one of {@link BatteryInformation#CELSIUS}
     * or {@link BatteryInformation#FAHRENHEIT}
     */
    public static int getDefaultTemperatureUnits(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getInt(DEFAULT_TEMPERATURE_UNITS, Locale.getDefault() == Locale.US
                ? BatteryInformation.FAHRENHEIT : BatteryInformation.CELSIUS);
    }

    /**
     * Set the user preferred temperature units
     *
     * @param ctx   the application context
     * @param units one of {@link BatteryInformation#CELSIUS}
     *              or {@link BatteryInformation#FAHRENHEIT}
     */
    public static void setDefaultTemperatureUnits(@NonNull final Context ctx, final int units) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putInt(DEFAULT_TEMPERATURE_UNITS, units);
        edit.commit();
    }

    /**
     * Get the user preferred song recognition application
     *
     * @param ctx the application context
     * @return one of {@link SongRecognitionProvider}
     */
    public static SongRecognitionProvider getDefaultSongRecognition(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return SongRecognitionProvider.getProvider(pref.getInt(DEFAULT_SONG_RECOGNITION,
                SongRecognitionProvider.UNKNOWN.ordinal()));
    }

    /**
     * Set the user preferred song recognition application
     *
     * @param ctx      the application context
     * @param provider {@link SongRecognitionProvider}
     */
    public static void setDefaultSongRecognition(@NonNull final Context ctx,
                                                 @NonNull final SongRecognitionProvider provider) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putInt(DEFAULT_SONG_RECOGNITION, provider.ordinal());
        edit.commit();
    }

    /**
     * Get the user assigned maximum speech cache size, which they can adjust in the Saiy
     * Application Settings
     *
     * @param ctx the application context
     * @return the maximum speech cache size
     */
    public static long getMaxSpeechCacheSize(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getLong(MAX_SPEECH_CACHE_SIZE, DBSpeech.MAX_CACHE_SIZE);
    }

    /**
     * Set the maximum speech cache size
     *
     * @param maxSize the maximum cache size
     * @param ctx     the application context
     */
    public static void setMaxSpeechCacheSize(@NonNull final Context ctx, final long maxSize) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putLong(MAX_SPEECH_CACHE_SIZE, maxSize);
        edit.commit();
    }

    /**
     * Get the last time the application was used
     *
     * @param ctx the application context
     * @return the last time the application was used
     */
    public static long getLastUsed(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getLong(LAST_USED, (long) ONE);
    }

    /**
     * Set the last time the application was used to now.
     *
     * @param ctx the application context
     */
    public static void setLastUsed(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putLong(LAST_USED, System.currentTimeMillis());
        edit.commit();

        SPH.incrementUsed(ctx);
    }

    /**
     * Get the number of speech requests that have been made, since the application data was last
     * wiped. This will be used to run various housekeeping tasks.
     *
     * @param ctx the application context
     * @return the count of application uses
     */
    public static long getUsedIncrement(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getLong(USED_INCREMENT, ONE);
    }

    /**
     * Increment the number of speech requests made
     *
     * @param ctx the application context
     */
    public static void incrementUsed(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putLong(USED_INCREMENT, (getUsedIncrement(ctx) + 1));
        edit.commit();
    }

    /**
     * Get the amount of times the user has been informed of this verbose information
     *
     * @param ctx the application context
     * @return the integer number of times
     */
    public static int getRemoteCommandVerbose(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getInt(REMOTE_COMMAND_VERBOSE, ZERO);
    }

    /**
     * Increment the number of times the user has been informed of this verbose information
     *
     * @param ctx the application context
     */
    public static void incrementRemoteCommandVerbose(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putInt(REMOTE_COMMAND_VERBOSE, (getRemoteCommandVerbose(ctx) + 1));
        edit.commit();
    }

    /**
     * Get the serialised user defined {@link Algorithm}
     *
     * @param ctx the application context
     * @return the double or default value
     */
    public static String getAlgorithms(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getString(ALGORITHMS, null);
    }

    /**
     * Set the user defined algorithms
     *
     * @param ctx        the application context
     * @param serialised list of {@link Algorithm}
     */
    public static void setAlgorithms(@NonNull final Context ctx, @NonNull final String serialised) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(ALGORITHMS, serialised);
        edit.commit();
    }

    /**
     * Get the upper distance limit to use in {@link SoundexHelper}
     *
     * @param ctx the application context
     * @return the double or default value
     */
    public static double getSoundexUpper(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return Double.valueOf(pref.getString(SOUNDEX_UPPER_THRESHOLD,
                String.valueOf(Algorithm.SOUNDEX_UPPER_THRESHOLD)));
    }

    /**
     * Set the upper distance limit to use in {@link SoundexHelper}
     *
     * @param ctx   the application context
     * @param limit to set
     */
    public static void setSoundexUpper(@NonNull final Context ctx, final double limit) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(SOUNDEX_UPPER_THRESHOLD, String.valueOf(limit));
        edit.commit();
    }

    /**
     * Get the upper distance limit to use in {@link NeedlemanWunschHelper}
     *
     * @param ctx the application context
     * @return the double or default value
     */
    public static double getNeedlemanWunschUpper(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return Double.valueOf(pref.getString(NW_UPPER_THRESHOLD, String.valueOf(Algorithm.NW_UPPER_THRESHOLD)));
    }

    /**
     * Set the upper distance limit to use in {@link NeedlemanWunschHelper}
     *
     * @param ctx   the application context
     * @param limit to set
     */
    public static void setNeedlemanWunschUpper(@NonNull final Context ctx, final double limit) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(NW_UPPER_THRESHOLD, String.valueOf(limit));
        edit.commit();
    }

    /**
     * Get the fuzzy multiplier to use in {@link FuzzyHelper}
     *
     * @param ctx the application context
     * @return the double or default value
     */
    public static double getFuzzyMultiplier(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return Double.valueOf(pref.getString(FUZZY_MULTIPLIER,
                String.valueOf(Algorithm.FUZZY_MULTIPLIER)));
    }

    /**
     * Set the fuzzy multiplier to use in {@link FuzzyHelper}
     *
     * @param ctx   the application context
     * @param limit to set
     */
    public static void setFuzzyMultiplier(@NonNull final Context ctx, final double limit) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(FUZZY_MULTIPLIER, String.valueOf(limit));
        edit.commit();
    }

    /**
     * Get the upper distance limit to use in {@link MongeElkanHelper}
     *
     * @param ctx the application context
     * @return the double or default value
     */
    public static double getMongeElkanUpper(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return Double.valueOf(pref.getString(ME_UPPER_THRESHOLD,
                String.valueOf(Algorithm.ME_UPPER_THRESHOLD)));
    }

    /**
     * Set the upper distance limit to use in {@link MongeElkanHelper}
     *
     * @param ctx   the application context
     * @param limit to set
     */
    public static void setMongeElkanUpper(@NonNull final Context ctx, final double limit) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(ME_UPPER_THRESHOLD, String.valueOf(limit));
        edit.commit();
    }


    /**
     * Get the upper distance limit to use in {@link LevenshteinHelper}
     *
     * @param ctx the application context
     * @return the double or default value
     */
    public static double getLevenshteinUpper(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return Double.valueOf(pref.getString(LEV_UPPER_THRESHOLD,
                String.valueOf(Algorithm.LEV_UPPER_THRESHOLD)));
    }

    /**
     * Set the upper distance limit to use in {@link LevenshteinHelper}
     *
     * @param ctx   the application context
     * @param limit to set
     */
    public static void setLevenshteinUpper(@NonNull final Context ctx, final double limit) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(LEV_UPPER_THRESHOLD, String.valueOf(limit));
        edit.commit();
    }

    /**
     * Get the upper distance limit to use in {@link JaroWinklerHelper}
     *
     * @param ctx the application context
     * @return the double or default value
     */
    public static double getJaroWinklerUpper(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return Double.valueOf(pref.getString(JWD_UPPER_THRESHOLD,
                String.valueOf(Algorithm.JWD_UPPER_THRESHOLD)));
    }

    /**
     * Get the lower distance limit to use in {@link JaroWinklerHelper}
     *
     * @param ctx the application context
     * @return the double or default value
     */
    public static double getJaroWinklerLower(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return Double.valueOf(pref.getString(JWD_LOWER_THRESHOLD,
                String.valueOf(Algorithm.JWD_LOWER_THRESHOLD)));
    }


    /**
     * Set the upper distance limit to use in {@link JaroWinklerHelper}
     *
     * @param ctx   the application context
     * @param limit to set
     */
    public static void setJaroWinklerUpper(@NonNull final Context ctx, final double limit) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(JWD_UPPER_THRESHOLD, String.valueOf(limit));
        edit.commit();
    }

    /**
     * Set the lower distance limit to use in {@link JaroWinklerHelper}
     *
     * @param ctx   the application context
     * @param limit to set
     */
    public static void setJaroWinklerLower(@NonNull final Context ctx, final double limit) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(JWD_LOWER_THRESHOLD, String.valueOf(limit));
        edit.commit();
    }

    /**
     * Get the default algorithm provider
     *
     * @param ctx the application context
     * @return the default algorithm provider
     */
    public static Algorithm getAlgorithm(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return Algorithm.valueOf(pref.getString(ALGORITHM, Algorithm.JARO_WINKLER.name()));
    }

    /**
     * Set the default algorithm provider
     *
     * @param ctx       the application context
     * @param algorithm to set as the default
     */
    public static void setAlgorithm(@NonNull final Context ctx, final @NonNull Algorithm algorithm) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(ALGORITHM, algorithm.name());
        edit.commit();
    }

    /**
     * Get the serialised string of the array of blacklisted applications which will be coerced into
     * a {@link ai.saiy.android.api.helper.BlackList} objects using {@link com.google.gson.Gson}
     *
     * @param ctx the application context
     * @return the serialised string or null
     */
    public static String getBlacklistArray(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getString(BLACKLIST, null);
    }

    /**
     * Set the serialised string of the blacklist array which has been coerced into
     * a {@link ai.saiy.android.api.helper.BlackList} objects using {@link com.google.gson.Gson}
     *
     * @param ctx       the application context
     * @param blacklist the serialised string
     */
    public static void setBlacklistArray(@NonNull final Context ctx, @Nullable final String blacklist) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(BLACKLIST, blacklist);
        edit.commit();
    }

    /**
     * Get the serialised string of the last ActivityRecognition which will be coerced into
     * a {@link Motion} object
     * using {@link com.google.gson.Gson}
     *
     * @param ctx the application context
     * @return the serialised string or null
     */
    public static String getMotion(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getString(MOTION, null);
    }

    /**
     * Set the serialised string of the last ActivityRecognition which has been coerced into
     * a {@link Motion} object
     * using {@link com.google.gson.Gson}
     *
     * @param ctx    the application context
     * @param motion the serialised string
     */
    public static void setMotion(@NonNull final Context ctx, @Nullable final String motion) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(MOTION, motion);
        edit.commit();
    }

    /**
     * Get the serialised string of the last emotion analysis which will be coerced into
     * a {@link ai.saiy.android.cognitive.emotion.provider.beyondverbal.AnalysisResult} object
     * using {@link com.google.gson.Gson}
     *
     * @param ctx the application context
     * @return the serialised string or null
     */
    public static String getEmotion(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getString(EMOTION, null);
    }

    /**
     * Set the serialised string of the last emotion analysis which has been coerced into
     * a {@link ai.saiy.android.cognitive.emotion.provider.beyondverbal.AnalysisResult} object
     * using {@link com.google.gson.Gson}
     *
     * @param ctx     the application context
     * @param emotion the serialised string
     */
    public static void setEmotion(@NonNull final Context ctx, @Nullable final String emotion) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(EMOTION, emotion);
        edit.commit();
    }

    /**
     * Get the serialised string of the Saiy accounts which will be coerced into
     * a {@link ai.saiy.android.user.SaiyAccountList} object using {@link com.google.gson.Gson}
     *
     * @param ctx the application context
     * @return the serialised string or null
     */
    public static String getSaiyAccounts(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getString(SAIY_ACCOUNTS, null);
    }

    /**
     * Set the serialised string of the Saiy accounts which has been coerced into
     * a {@link ai.saiy.android.user.SaiyAccountList} object using {@link com.google.gson.Gson}
     *
     * @param ctx         the application context
     * @param accountList the serialised string
     */
    public static void setSaiyAccounts(@NonNull final Context ctx, @Nullable final String accountList) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(SAIY_ACCOUNTS, accountList);
        edit.commit();
    }

    /**
     * Get the serialised string of the last action Saiy performed which will be coerced into
     * a {@link Memory} object using {@link com.google.gson.Gson}
     *
     * @param ctx the application context
     * @return the serialised string or null
     */
    public static String getMemory(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getString(MEMORY, null);
    }

    /**
     * Set the serialised string of the last action Saiy performed which has been coerced into
     * a {@link Memory} object using {@link com.google.gson.Gson}
     *
     * @param ctx    the application context
     * @param memory the serialised string
     */
    public static void setMemory(@NonNull final Context ctx, @NonNull final String memory) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(MEMORY, memory);
        edit.commit();
    }

    /**
     * Get the serialised string of the user's default Text to Speech Voice which will be coerced into
     * a {@link android.speech.tts.Voice} object using {@link com.google.gson.Gson}
     *
     * @param ctx the application context
     * @return the serialised string or null
     */
    public static String getDefaultTTSVoice(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getString(DEFAULT_TTS_VOICE, null);
    }

    /**
     * Set the serialised string of the user's default Text to Speech Voice which has been coerced into
     * a {@link android.speech.tts.Voice} object using {@link com.google.gson.Gson}
     *
     * @param ctx   the application context
     * @param voice the serialised string
     */
    public static void setDefaultTTSVoice(@NonNull final Context ctx, @Nullable final String voice) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(DEFAULT_TTS_VOICE, voice);
        edit.commit();
    }

    /**
     * Get the default translation provider
     *
     * @param ctx the application context
     * @return the default translation provider
     */
    public static int getDefaultTranslationProvider(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getInt(TRANSLATION_PROVIDER, TranslationProvider.TRANSLATION_PROVIDER_GOOGLE);
    }

    /**
     * Set the default translation provider
     *
     * @param ctx      the application context
     * @param provider of the translation
     */
    public static void setDefaultTranslationProvider(@NonNull final Context ctx, final int provider) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putInt(TRANSLATION_PROVIDER, provider);
        edit.commit();
    }

    /**
     * Set the Bing refresh token timeout
     *
     * @param ctx  the application context
     * @param time the {@link System#currentTimeMillis()}
     */
    public static void setBingTokenExpiryTime(@NonNull final Context ctx, final long time) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);
        edit.putLong(BING_OAUTH_UPDATE, time);
        edit.commit();
    }

    /**
     * Get the Bing refresh token timeout
     *
     * @param ctx the application context
     * @return the time the token was last refreshed
     */
    public static long getBingTokenExpiryTime(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getLong(BING_OAUTH_UPDATE, 0);
    }

    /**
     * Get the Bing access token
     *
     * @param ctx the application context
     * @return the Bing access token or an empty String
     */
    public static String getBingToken(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getString(BING_TOKEN, null);
    }

    /**
     * Set the Bing access token
     *
     * @param ctx   the application context
     * @param token the Bing access token
     */
    public static void setBingToken(@NonNull final Context ctx, @NonNull final String token) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(BING_TOKEN, token);
        edit.commit();
    }

    /**
     * Get the serialised string of the most recent token credentials which will be coerced into
     * an {@link BVCredentials}
     * object using {@link com.google.gson.Gson}
     *
     * @param ctx the application context
     * @return the serialised string or null
     */
    public static String getBeyondVerbalCredentials(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getString(BEYOND_VERBAL_AUTH_RESPONSE, null);
    }

    /**
     * Set the serialised string of the last token credentials which has been coerced into
     * a {@link BVCredentials}
     * object using {@link com.google.gson.Gson}
     *
     * @param ctx         the application context
     * @param credentials the serialised string
     */
    public static void setBeyondVerbalCredentials(@NonNull final Context ctx, @NonNull final String credentials) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(BEYOND_VERBAL_AUTH_RESPONSE, credentials);
        edit.commit();
    }

    /**
     * Get the user assigned pause timeout
     *
     * @param ctx the application context
     * @return the pause timeout
     */
    public static long getPauseTimeout(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getLong(PAUSE_TIMEOUT, RecognitionNative.PAUSE_TIMEOUT);
    }

    /**
     * Set the user assigned pause timeout
     *
     * @param ctx     the application context
     * @param timeout to assign
     */
    public static void setPauseTimeout(@NonNull final Context ctx, final Long timeout) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putLong(PAUSE_TIMEOUT, timeout);
        edit.commit();
    }

    /**
     * Set that the user has seen the what's new note.
     *
     * @param ctx the application context
     */
    public static void setWhatsNew(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(WHATS_NEW, true);
        edit.commit();
    }

    /**
     * Get whether or not the user has seen the what's new note.
     *
     * @param ctx the application context
     * @return true if the user has seen the what's new note, false otherwise
     */
    public static boolean getWhatsNew(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(WHATS_NEW, false);
    }

    /**
     * Set that the user has already heard the verbose enrollment explanation.
     *
     * @param ctx the application context
     */
    public static void setEnrollmentVerbose(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(ENROLLMENT_VERBOSE, true);
        edit.commit();
    }

    /**
     * Get if the user has already heard the verbose enrollment explanation.
     *
     * @param ctx the application context
     * @return true if the user has heard the verbose explanation, false otherwise
     */
    public static boolean getEnrollmentVerbose(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(ENROLLMENT_VERBOSE, false);
    }

    /**
     * Set that the user has seen the developer note.
     *
     * @param ctx the application context
     */
    public static void setDeveloperNote(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(DEVELOPER_NOTE, true);
        edit.commit();
    }

    /**
     * Get whether or not the user has seen the developer note.
     *
     * @param ctx the application context
     * @return true if the user has seen the developer note, false otherwise
     */
    public static boolean getDeveloperNote(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(DEVELOPER_NOTE, false);
    }

    /**
     * Set that the user has accepted the disclaimer.
     *
     * @param ctx the application context
     */
    public static void setAcceptedDisclaimer(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(DISCLAIMER, true);
        edit.commit();
    }

    /**
     * Get whether or not the user has accepted the disclaimer.
     *
     * @param ctx the application context
     * @return true if the disclaimer has been accepted, false otherwise
     */
    public static boolean getAcceptedDisclaimer(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(DISCLAIMER, false);
    }

    /**
     * Set whether or not to toast unknown commands.
     *
     * @param ctx       the application context
     * @param condition to be applied.
     */
    public static void setToastUnknown(@NonNull final Context ctx, final boolean condition) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(TOAST_UNKNOWN, condition);
        edit.commit();
    }

    /**
     * Get whether or not to toast unknown commands.
     *
     * @param ctx the application context
     * @return true if commands should be toasted, false otherwise
     */
    public static boolean getToastUnknown(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(TOAST_UNKNOWN, true);
    }

    /**
     * Set whether or not to track the user's motion.
     *
     * @param ctx       the application context
     * @param condition to be applied.
     */
    public static void setMotionEnabled(@NonNull final Context ctx, final boolean condition) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(MOTION_ENABLED, condition);
        edit.commit();
    }

    /**
     * Get whether or not to track the user's motion
     *
     * @param ctx the application context
     * @return true if motion should be tracked, false otherwise
     */
    public static boolean getMotionEnabled(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(MOTION_ENABLED, true);
    }

    /**
     * Set whether or not to start the {@link SelfAware} service is enabled by the user.
     *
     * @param ctx       the application context
     * @param condition to be applied.
     */
    public static void setSelfAwareEnabled(@NonNull final Context ctx, final boolean condition) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(SELF_AWARE_ENABLED, condition);
        edit.commit();
    }

    /**
     * Get whether or not to start the {@link SelfAware} service is enabled by the user.
     *
     * @param ctx the application context
     * @return true if the {@link SelfAware} should be enabled, false otherwise
     */
    public static boolean getSelfAwareEnabled(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(SELF_AWARE_ENABLED, true);
    }

    /**
     * Set whether or not to start the {@link SelfAware} service at boot.
     *
     * @param ctx       the application context
     * @param condition to be applied.
     */
    public static void setStartAtBoot(@NonNull final Context ctx, final boolean condition) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(BOOT_START, condition);
        edit.commit();
    }

    /**
     * Check if the {@link SelfAware} should start at boot
     *
     * @param ctx the application context
     * @return true if the {@link SelfAware} should start at boot, false otherwise
     */
    public static boolean getStartAtBoot(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(BOOT_START, true);
    }

    /**
     * Get the default Text to Speech provider
     *
     * @param ctx the application context
     * @return the default Text to Speech provider
     */
    public static SaiyDefaults.TTS getDefaultTTS(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return SaiyDefaults.getProviderTTS(pref.getString(DEFAULT_TTS, SaiyDefaults.TTS.LOCAL.name()));
    }

    /**
     * Set the default Text to Speech provider
     *
     * @param ctx      the application context
     * @param provider of the Text to Speech
     */
    public static void setDefaultTTS(@NonNull final Context ctx, @NonNull final SaiyDefaults.TTS provider) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(DEFAULT_TTS, provider.name());
        edit.commit();
    }

    /**
     * Get the user preferred Text to Speech voice gender
     *
     * @param ctx the application context
     * @return one of {@link Gender#FEMALE} or {@link Gender#MALE}
     */
    public static Gender getDefaultTTSGender(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return Gender.getGender(pref.getString(DEFAULT_TTS_GENDER, Gender.FEMALE.name()));
    }

    /**
     * Set the user preferred Text to Speech voice gender
     *
     * @param ctx    the application context
     * @param gender one of {@link Gender#FEMALE} or {@link Gender#MALE}
     */
    public static void setDefaultTTSGender(@NonNull final Context ctx, @NonNull final Gender gender) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(DEFAULT_TTS_GENDER, gender.name());
        edit.commit();
    }

    /**
     * Set whether the user wishes their voice data to be subject to emotion analysis
     *
     * @param ctx       the application context
     * @param condition to apply
     */
    public static void setEmotionPermission(@NonNull final Context ctx, final boolean condition) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(EMOTION_PERMISSION, condition);
        edit.commit();
    }

    /**
     * Get whether the user wishes their voice data to be subject to emotion analysis
     *
     * @param ctx the application context
     * @return true if they require emotion analysis
     */
    public static boolean getEmotionPermission(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(EMOTION_PERMISSION, false);
    }

    /**
     * Set the default {@link Locale} of the voice recognition
     *
     * @param ctx          the application context
     * @param nativeLocale to apply as the default.
     */
    public static void setVRLocale(@NonNull final Context ctx, @NonNull final Locale nativeLocale) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(VR_LOCALE, nativeLocale.toString());
        edit.commit();
    }

    /**
     * Get the default {@link Locale} of the voice recognition
     *
     * @param ctx the application context
     * @return the default {@link Locale} of the recognition
     */
    public static Locale getVRLocale(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return UtilsLocale.stringToLocale(pref.getString(VR_LOCALE, UtilsLocale.DEFAULT_LOCALE_STRING));
    }

    /**
     * Set the default {@link Locale} of the Text to Speech engine
     *
     * @param ctx          the application context
     * @param nativeLocale to apply as the default.
     */
    public static void setTTSLocale(@NonNull final Context ctx, @NonNull final Locale nativeLocale) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(TTS_LOCALE, nativeLocale.toString());
        edit.commit();
    }

    /**
     * Get the default {@link Locale} of the Text to Speech engine
     *
     * @param ctx the application context
     * @return the default {@link Locale} of the engine
     */
    public static Locale getTTSLocale(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return UtilsLocale.stringToLocale(pref.getString(TTS_LOCALE, UtilsLocale.DEFAULT_LOCALE_STRING));
    }

    /**
     * Get the user preferred inactivity timeout
     *
     * @param ctx the application context
     * @return the value in milliseconds
     */
    public static long getInactivityTimeout(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getLong(INACTIVITY_TIMEOUT, SelfAwareConditions.DEFAULT_INACTIVITY_TIMEOUT);
    }

    /**
     * Set the user preferred inactivity timeout, to release memory resources
     *
     * @param ctx     the application context
     * @param timeout in milliseconds
     */
    public static void setInactivityTimeout(@NonNull final Context ctx, final long timeout) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putLong(INACTIVITY_TIMEOUT, timeout);
        edit.commit();
    }

    /**
     * Get the amount of times the user has been informed of this verbose information
     *
     * @param ctx the application context
     * @return the integer number of times
     */
    public static int getSpellCommandVerbose(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getInt(SPELL_COMMAND_VERBOSE, ZERO);
    }

    /**
     * Increment the number of times the user has been informed of this verbose information
     *
     * @param ctx the application context
     */
    public static void incrementSpellCommandVerbose(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putInt(SPELL_COMMAND_VERBOSE, (getSpellCommandVerbose(ctx) + 1));
        edit.commit();
    }

    /**
     * Get the amount of times the user has been informed of this verbose information
     *
     * @param ctx the application context
     * @return the integer number of times
     */
    public static int getTranslateCommandVerbose(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getInt(TRANSLATE_COMMAND_VERBOSE, ZERO);
    }

    /**
     * Increment the number of times the user has been informed of this verbose information
     *
     * @param ctx the application context
     */
    public static void incrementTranslateCommandVerbose(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putInt(TRANSLATE_COMMAND_VERBOSE, (getTranslateCommandVerbose(ctx) + 1));
        edit.commit();
    }

    /**
     * Set the user's preferred vibration condition
     *
     * @param ctx       the application context
     * @param condition to set
     */
    public static void setVibrateCondition(@NonNull final Context ctx, final boolean condition) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(VIBRATE, condition);
        edit.commit();
    }

    /**
     * Get the user's preferred vibration condition
     *
     * @param ctx the application context
     * @return true if the user requires haptic feedback
     */
    public static boolean getVibrateCondition(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(VIBRATE, true);
    }

    /**
     * Set the user's preference for how long a 'ping' request should be given to timeout.
     *
     * @param ctx     the application context
     * @param timeout in milliseconds to be applied
     */
    public static void setPingTimeout(@NonNull final Context ctx, final int timeout) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putInt(PING_TIMEOUT, timeout);
        edit.commit();
    }

    /**
     * Get the user's preference for how long a 'ping' request should be given to timeout.
     *
     * @param ctx the application context
     * @return the timeout in milliseconds
     */
    public static int getPingTimeout(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getInt(PING_TIMEOUT, Network.PING_TIMEOUT);
    }

    /**
     * Set the user's preference for intercepting the Google Now commands.
     *
     * @param ctx       the application context
     * @param condition to be applied
     */
    public static void setInterceptGoogle(@NonNull final Context ctx, final boolean condition) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(INTERCEPT_GOOGLE_NOW, condition);
        edit.commit();
    }

    /**
     * Get the user's preference for intercepting the Google Now commands.
     *
     * @param ctx the application context
     * @return true if Google Now commands should be intercepted, false otherwise
     */
    public static boolean getInterceptGoogle(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(INTERCEPT_GOOGLE_NOW, false);
    }

    /**
     * Set the user's preference for announcing notifications.
     *
     * @param ctx       the application context
     * @param condition to be applied
     */
    public static void setAnnounceNotifications(@NonNull final Context ctx, final boolean condition) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(ANNOUNCE_NOTIFICATIONS, condition);
        edit.commit();
    }

    /**
     * Get the user's preference for announcing notifications.
     *
     * @param ctx the application context
     * @return true if notification content should be announced, false otherwise
     */
    public static boolean getAnnounceNotifications(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(ANNOUNCE_NOTIFICATIONS, false);
    }

    /**
     * Set the user's preference for pinging the network prior to making a request.
     *
     * @param ctx       the application context
     * @param condition to be applied
     */
    public static void setPingCheck(@NonNull final Context ctx, final boolean condition) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(PING_CHECK, condition);
        edit.commit();
    }

    /**
     * Get the user's preference for pinging the network prior to making a request.
     *
     * @param ctx the application context
     * @return true if the network should be pinged, false otherwise
     */
    public static boolean getPingCheck(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(PING_CHECK, true);
    }

    /**
     * Set the user's preference for hotword wakelock.
     *
     * @param ctx       the application context
     * @param condition to be applied
     */
    public static void setHotwordWakelock(@NonNull final Context ctx, final boolean condition) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(HOTWORD_WAKELOCK, condition);
        edit.commit();
    }

    /**
     * Get the user's preference for hotword wakelock.
     *
     * @param ctx the application context
     * @return true if hotword should hold a wakelock
     */
    public static boolean getHotwordWakelock(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(HOTWORD_WAKELOCK, false);
    }

    /**
     * Set the user's preference for hotword when driving.
     *
     * @param ctx       the application context
     * @param condition to be applied
     */
    public static void setHotwordDriving(@NonNull final Context ctx, final boolean condition) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(HOTWORD_DRIVING, condition);
        edit.commit();
    }

    /**
     * Get the user's preference for hotword when driving.
     *
     * @param ctx the application context
     * @return true if hotword should begin when driving
     */
    public static boolean getHotwordDriving(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(HOTWORD_DRIVING, false);
    }

    /**
     * Set the user's preference for hotword security.
     *
     * @param ctx       the application context
     * @param condition to be applied
     */
    public static void setHotwordSecure(@NonNull final Context ctx, final boolean condition) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(HOTWORD_SECURE, condition);
        edit.commit();
    }

    /**
     * Get the user's preference for hotword security.
     *
     * @param ctx the application context
     * @return true if hotword commands should be secure
     */
    public static boolean getHotwordSecure(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(HOTWORD_SECURE, true);
    }

    /**
     * Set the user's preference for hotword detection starting at boot.
     *
     * @param ctx       the application context
     * @param condition to be applied
     */
    public static void setHotwordBoot(@NonNull final Context ctx, final boolean condition) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(HOTWORD_BOOT, condition);
        edit.commit();
    }

    /**
     * Get the user's preference for hotword detection starting at boot.
     *
     * @param ctx the application context
     * @return true if hotword detection should start at boot
     */
    public static boolean getHotwordBoot(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(HOTWORD_BOOT, false);
    }

    /**
     * Set the user's preference for offline voice recognition.
     *
     * @param ctx       the application context
     * @param condition to be applied
     */
    public static void setUseOffline(@NonNull final Context ctx, final boolean condition) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(USE_OFFLINE, condition);
        edit.commit();
    }

    /**
     * Get the user's preference for offline voice recognition.
     *
     * @param ctx the application context
     * @return true if offline recognition is preferred
     */
    public static boolean getUseOffline(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(USE_OFFLINE, false);
    }

    /**
     * Set the user's preference for a network synthesised voice.
     *
     * @param ctx       the application context
     * @param condition to be applied
     */
    public static void setNetworkSynthesis(@NonNull final Context ctx, final boolean condition) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(NETWORK_SYNTHESIS, condition);
        edit.commit();
    }

    /**
     * Get the user's preference for a network synthesised voice.
     *
     * @param ctx the application context
     * @return true if network synthesis is preferred
     */
    public static boolean getNetworkSynthesis(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(NETWORK_SYNTHESIS, true);
    }

    /**
     * Get the user default action for an unknown command
     *
     * @param ctx the application context
     * @return the action constant
     */
    public static int getCommandUnknownAction(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getInt(COMMAND_UNKNOWN_ACTION, Unknown.UNKNOWN_STATE);
    }

    /**
     * Set the user default action for an unknown command
     *
     * @param ctx    the application context
     * @param action to set
     */
    public static void setCommandUnknownAction(@NonNull final Context ctx, final int action) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putInt(COMMAND_UNKNOWN_ACTION, action);
        edit.commit();
    }

    /**
     * Get the minimum require connection level to process network events
     *
     * @param ctx the application context
     * @return the integer constant from {@link Network}
     */
    public static int getConnectionMinimum(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getInt(CONNECTION_MINIMUM, Network.CONNECTION_TYPE_3G);
    }

    /**
     * Set the minimum require connection level to process network events
     *
     * @param ctx        the application context
     * @param connection to set as a minimum
     */
    public static void setConnectionMinimum(@NonNull final Context ctx, final int connection) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putInt(CONNECTION_MINIMUM, connection);
        edit.commit();
    }

    /**
     * Get the hotword the user has enrolled.
     *
     * @param ctx the application context
     * @return the enrolled hotword or null if one has yet to be enrolled
     */
    public static String getHotword(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getString(HOTWORD, SaiyRequestParams.SILENCE);
    }

    /**
     * Set the user's enrolled hotword
     *
     * @param ctx     the application context
     * @param hotword that has been enrolled
     */
    public static void setHotword(@NonNull final Context ctx, final String hotword) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(HOTWORD, hotword);
        edit.commit();
    }

    /**
     * Get the user defined custom intro
     *
     * @param ctx the application context
     * @return the user's name or 'master' if none is applied
     */
    public static String getCustomIntro(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getString(CUSTOM_INTRO, null);
    }

    /**
     * Set the user defined custom intro
     *
     * @param ctx   the application context
     * @param intro by which they wish to be called
     */
    public static void setCustomIntro(@NonNull final Context ctx, final String intro) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(CUSTOM_INTRO, intro);
        edit.commit();
    }

    /**
     * Set the user's preference for randomising their custom intro.
     *
     * @param ctx       the application context
     * @param condition to be applied
     */
    public static void setCustomIntroRandom(@NonNull final Context ctx, final boolean condition) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(CUSTOM_INTRO_RANDOM, condition);
        edit.commit();
    }

    /**
     * Get the user's preference for randomising their custom intro.
     *
     * @param ctx the application context
     * @return true if offline recognition is preferred
     */
    public static boolean getCustomIntroRandom(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(CUSTOM_INTRO_RANDOM, true);
    }

    /**
     * Get the user defined name by which they wish to be addressed.
     *
     * @param ctx the application context
     * @return the user's name or 'master' if none is applied
     */
    public static String getUserName(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getString(USER_NAME, ctx.getString(ai.saiy.android.R.string.master));
    }

    /**
     * Set the user defined name by which they wish to be addressed.
     *
     * @param ctx      the application context
     * @param userName by which they wish to be called
     */
    public static void setUserName(@NonNull final Context ctx, final String userName) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(USER_NAME, userName);
        edit.commit();
    }

    /**
     * Set whether the recogniser should use a workaround
     *
     * @param ctx       the application context
     * @param condition to apply
     */
    public static void setRecogniserBusyFix(@NonNull final Context ctx, final boolean condition) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(RECOGNISER_BUSY_FIX, condition);
        edit.commit();
    }

    /**
     * Get whether the recogniser should use a workaround
     *
     * @param ctx the application context
     * @return true as the default, or false if the user has disabled this
     */
    public static boolean getRecogniserBusyFix(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(RECOGNISER_BUSY_FIX, Installed.isGoogleNowLauncherDefault(ctx));
    }

    /**
     * Set whether the hotword should use a workaround
     *
     * @param ctx       the application context
     * @param condition to apply
     */
    public static void setOkayGoogleFix(@NonNull final Context ctx, final boolean condition) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(OKAY_GOOGLE_FIX, condition);
        edit.commit();
    }

    /**
     * Get whether the hotword should use a workaround
     *
     * @param ctx the application context
     * @return true as the default, or false if the user has disabled this
     */
    public static boolean getOkayGoogleFix(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(OKAY_GOOGLE_FIX, Installed.isGoogleNowLauncherDefault(ctx));
    }
}
