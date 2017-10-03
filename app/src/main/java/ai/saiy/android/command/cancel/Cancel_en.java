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

package ai.saiy.android.command.cancel;

import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SaiyResources;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.recognition.provider.android.RecognitionNative;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsBundle;
import ai.saiy.android.utils.UtilsList;

/**
 * Class to check if the user wishes to cancel the voice interaction.
 * <p/>
 * Created by benrandall76@gmail.com on 17/02/2016.
 */
public class Cancel_en {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = Cancel_en.class.getSimpleName();

    private static String cancel_;
    private static String cancel_trim;
    private static String council_;
    private static String council_trim;
    private static String cancel_that;
    private static String never_mind;
    private static String shush;
    private static String shut_up;

    private SupportedLanguage sl;
    private ArrayList<String> voiceData;
    private float[] confidence;

    /**
     * Constructor
     *
     * @param sr    the {@link SaiyResources}
     * @param reset true if the {@link SaiyResources} should be reset immediately
     */
    public Cancel_en(@NonNull final SaiyResources sr, final boolean reset) {

        if (cancel_ == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "strings initialised");
            }
        }

        if (reset) {
            sr.reset();
        }
    }

    private static void initStrings(@NonNull final SaiyResources sr) {
        cancel_ = sr.getString(ai.saiy.android.R.string.cancel_);
        cancel_trim = cancel_.trim();
        council_ = sr.getString(ai.saiy.android.R.string.council_);
        council_trim = council_.trim();
        cancel_that = sr.getString(ai.saiy.android.R.string.cancel_that);
        never_mind = sr.getString(ai.saiy.android.R.string.never_mind);
        shush = sr.getString(ai.saiy.android.R.string.shush);
        shut_up = sr.getString(ai.saiy.android.R.string.shut_up);
    }

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
    public Cancel_en(@NonNull final SaiyResources sr, @NonNull final SupportedLanguage sl,
                     @NonNull final ArrayList<String> voiceData, @NonNull float[] confidence) {

        this.sl = sl;
        this.voiceData = voiceData;
        this.confidence = confidence;

        if (cancel_ == null) {
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

                if (vdLower.startsWith(cancel_)
                        || vdLower.matches(cancel_trim)
                        || vdLower.contains(cancel_ + cancel_trim)
                        || vdLower.endsWith(cancel_that)
                        || vdLower.matches(never_mind)
                        || vdLower.matches(shush)
                        || vdLower.matches(shut_up)
                        || vdLower.startsWith(council_)
                        || vdLower.matches(council_trim)
                        || vdLower.contains(council_ + council_trim)
                        || vdLower.contains(council_ + cancel_trim)
                        || vdLower.contains(cancel_ + council_trim)) {

                    toReturn.add(new Pair<>(CC.COMMAND_CANCEL, confidence[i]));
                }
            }
        }

        if (DEBUG) {
            MyLog.i(CLS_NAME, "isCancel: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }

        return toReturn;
    }

    /**
     * Iterate through the voice data array to see if the user has requested to cancel the current
     * speech recognition session.
     * <p/>
     * Any unstable results that are found, may only contain one word or be a segment of the middle
     * of the utterance and therefore we have to apply different matching in order to avoid a false positive.
     * <p/>
     * Note - As the speech array will never contain more than ten entries, perhaps implementing a
     * matcher, would probably be overkill.
     *
     * @param loc     the {@link SupportedLanguage} {@link Locale}
     * @param results {@link Bundle} containing the voice data
     * @return true if a cancel variant is detected
     */
    public boolean detectPartial(@NonNull final Locale loc, @NonNull final Bundle results) {

        final long then = System.nanoTime();
        boolean cancelled = false;

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

                            if (vdLower.startsWith(cancel_)
                                    || vdLower.matches(cancel_trim)
                                    || vdLower.contains(cancel_ + cancel_trim)
                                    || vdLower.endsWith(cancel_that)
                                    || vdLower.matches(never_mind)
                                    || vdLower.matches(shush)
                                    || vdLower.matches(shut_up)
                                    || vdLower.startsWith(council_)
                                    || vdLower.matches(council_trim)
                                    || vdLower.contains(council_ + council_trim)
                                    || vdLower.contains(council_ + cancel_trim)
                                    || vdLower.contains(cancel_ + council_trim)) {

                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "partial vd: " + vdLower);
                                }
                                cancelled = true;
                                break;
                            }
                        }
                    }
                }

                if (!cancelled) {
                    final ArrayList<String> unstableData = results.getStringArrayList(RecognitionNative.UNSTABLE_RESULTS);

                    /* handles empty string bug */
                    if (UtilsList.notNaked(unstableData)) {
                        unstableData.removeAll(Collections.singleton(""));

                        if (!unstableData.isEmpty()) {

                            String vdLower;
                            int size = unstableData.size();
                            for (int i = 0; i < size; i++) {
                                vdLower = unstableData.get(i).toLowerCase(loc).trim();

                                if (vdLower.contains(cancel_ + cancel_trim)
                                        || vdLower.endsWith(cancel_that)
                                        || vdLower.matches(never_mind)
                                        || vdLower.matches(shush)
                                        || vdLower.matches(shut_up)
                                        || vdLower.contains(council_ + council_trim)
                                        || vdLower.contains(council_ + cancel_trim)
                                        || vdLower.contains(cancel_ + council_trim)) {

                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "unstable vd: " + vdLower);
                                    }
                                    cancelled = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            } else {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "isCancel: bundle has been tampered with");
                }
            }
        }

        if (DEBUG) {
            MyLog.i(CLS_NAME, "isCancel: returning ~ " + cancelled);
            MyLog.getElapsed(CLS_NAME, then);
        }

        return cancelled;
    }

    public boolean detectCancel(@NonNull final Locale loc, @NonNull final ArrayList<String> voiceData) {

        String vdLower;
        for (final String utterance : voiceData) {
            vdLower = utterance.toLowerCase(loc).trim();

            if (vdLower.matches(cancel_trim)
                    || vdLower.contains(cancel_ + cancel_trim)
                    || vdLower.endsWith(cancel_that)
                    || vdLower.matches(never_mind)
                    || vdLower.matches(shush)
                    || vdLower.matches(shut_up)
                    || vdLower.contains(council_ + council_trim)
                    || vdLower.contains(council_ + cancel_trim)
                    || vdLower.contains(cancel_ + council_trim)) {
                return true;
            }
        }

        return false;
    }
}