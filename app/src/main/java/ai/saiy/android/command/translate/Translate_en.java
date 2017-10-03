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

package ai.saiy.android.command.translate;

import android.content.Context;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.command.translate.provider.bing.TranslationLanguageBing;
import ai.saiy.android.localisation.SaiyResources;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.recognition.provider.android.RecognitionNative;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsBundle;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsString;

/**
 * Helper class to resolve translation commands
 * <p/>
 * Created by benrandall76@gmail.com on 17/04/2016.
 */
public class Translate_en {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = Translate_en.class.getSimpleName();

    private SupportedLanguage sl;
    private ArrayList<String> voiceData;
    private float[] confidence;

    private static String translate_;

    /**
     * Constructor
     * <p/>
     * Used by a {@link Callable} to prepare everything that is need when
     * {@link Callable#call()} is executed with the {@link SaiyResources} managed elsewhere.
     *
     * @param sr         the {@link SaiyResources}
     * @param sl         the {@link SupportedLanguage}
     * @param voiceData  the array of voice data
     * @param confidence the array of confidence scores
     */
    public Translate_en(@NonNull final SaiyResources sr, @NonNull final SupportedLanguage sl,
                        @NonNull final ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = sl;
        this.voiceData = voiceData;
        this.confidence = confidence;

        if (translate_ == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "strings initialised");
            }
        }
    }

    private static void initStrings(@NonNull final SaiyResources sr) {
        translate_ = sr.getString(ai.saiy.android.R.string.translate_);
    }

    /**
     * Constructor
     *
     * @param sr    the {@link SaiyResources}
     * @param reset true if the {@link SaiyResources} should be reset immediately
     */
    public Translate_en(@NonNull final SaiyResources sr, final boolean reset) {
        translate_ = sr.getString(ai.saiy.android.R.string.translate_);

        if (reset) {
            sr.reset();
        }
    }

    /**
     * Iterate through the voice data array to see if the user has requested to cancel the current
     * speech recognition session.
     * <p/>
     * Note - As the speech array will never contain more than ten entries, to consider the static
     * nature and performance issues here, perhaps implementing a matcher, would probably be overkill.
     *
     * @return an Array list of Pairs containing the {@link CC} and float confidence
     */
    public ArrayList<Pair<CC, Float>> detectCallable() {

        final long then = System.nanoTime();
        final ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();

        if (UtilsList.notNaked(voiceData) && UtilsList.notNaked(confidence)
                && voiceData.size() == confidence.length) {

            final Locale loc = sl.getLocale();

            String vdLower;
            int size = voiceData.size();
            for (int i = 0; i < size; i++) {
                vdLower = voiceData.get(i).toLowerCase(loc).trim();

                if (vdLower.contains(translate_)) {
                    toReturn.add(new Pair<>(CC.COMMAND_TRANSLATE, confidence[i]));
                }
            }
        }

        if (DEBUG) {
            MyLog.i(CLS_NAME, "translate: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }

        return toReturn;
    }

    /**
     * Iterate through the voice data array to see if the user has requested a possible translation
     * command.
     * <p/>
     * Note - As the speech array will never contain more than ten entries, perhaps implementing a
     * matcher, would probably be overkill.
     *
     * @param results {@link Bundle} containing the voice data
     * @param loc     the {@link SupportedLanguage} {@link Locale}
     * @return true if a command variant is detected
     */
    public boolean detectPartial(@NonNull final Locale loc, @NonNull final Bundle results) {

        final long then = System.nanoTime();
        boolean translate = false;

        if (UtilsBundle.notNaked(results)) {
            if (!UtilsBundle.isSuspicious(results)) {

                final ArrayList<String> partialData = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                /* handles empty string bug */
                if (UtilsList.notNaked(partialData)) {
                    partialData.removeAll(Collections.singleton(""));

                    if (!partialData.isEmpty()) {

                        String vdLower;
                        int size = partialData.size();
                        for (int i = 0; i < size; i++) {
                            vdLower = partialData.get(i).toLowerCase(loc).trim();
                            if (vdLower.startsWith(translate_)) {

                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "partial vd: " + vdLower);
                                }
                                translate = true;
                                break;
                            }
                        }
                    }
                }

                if (!translate) {

                    final ArrayList<String> unstableData = results.getStringArrayList(RecognitionNative.UNSTABLE_RESULTS);

                    /* handles empty string bug */
                    if (UtilsList.notNaked(unstableData)) {
                        unstableData.removeAll(Collections.singleton(""));

                        if (!unstableData.isEmpty()) {

                            String vdLower;
                            int size = unstableData.size();
                            for (int i = 0; i < size; i++) {
                                vdLower = unstableData.get(i).toLowerCase(loc).trim();
                                if (vdLower.startsWith(translate_)) {

                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "unstable vd: " + vdLower);
                                    }
                                    translate = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            } else {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "isTranslate: bundle has been tampered with");
                }
            }
        }

        if (DEBUG) {
            MyLog.i(CLS_NAME, "isTranslate: returning ~ " + translate);
            MyLog.getElapsed(CLS_NAME, then);
        }

        return translate;
    }

    /**
     * @param ctx       the application context
     * @param utterance the utterance
     * @param language  the detected language one of {@link TranslationLanguageBing}
     * @param sl        the {@link SupportedLanguage}
     * @return the prepared text ready for the command
     */
    public static String resolveBody(@NonNull final Context ctx, @NonNull String utterance,
                                     @NonNull final String language, @NonNull final SupportedLanguage sl) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "resolveBody: " + utterance);
        }

        final SaiyResources sr = new SaiyResources(ctx, sl);
        final String in_to = sr.getString(ai.saiy.android.R.string.in_to);
        final String into = sr.getString(ai.saiy.android.R.string.into);
        final String to = sr.getString(ai.saiy.android.R.string.to);

        if (translate_ == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "strings initialised");
            }
        }

        sr.reset();

        utterance = utterance.replaceFirst(translate_, "");

        if (utterance.endsWith(language)) {

            if (utterance.endsWith(in_to + " " + language)) {
                utterance = utterance.replace(in_to + " " + language, "").trim();
            } else if (utterance.endsWith(into + " " + language)) {
                utterance = utterance.replace(into + " " + language, "").trim();
            } else if (utterance.endsWith(to + " " + language)) {
                utterance = utterance.replace(to + " " + language, "").trim();
            } else {
                utterance = utterance.replaceFirst("(?s)(.*)" + language, "$1" + "");
            }

            if (DEBUG) {
                MyLog.i(CLS_NAME, "resolveBody: returning: " + utterance);
            }

            return utterance;

        } else {

            final String[] strips = utterance.split(language);

            if (strips.length > 1 && UtilsString.notNaked(strips[1])) {

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "resolveBody: returning: " + strips[1].trim());
                }

                return strips[1].trim();
            }
        }

        return null;
    }
}
