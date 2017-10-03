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

package ai.saiy.android.command.emotion;

import android.support.annotation.NonNull;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SaiyResources;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;

/**
 * Helper class to emotion analysis commands.
 * <p/>
 * Created by benrandall76@gmail.com on 06/04/2016.
 */
public class Emotion_en {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = Emotion_en.class.getSimpleName();

    private static String emotion;
    private static String feeling;

    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public Emotion_en(@NonNull final SaiyResources sr, @NonNull final SupportedLanguage sl,
                      @NonNull final ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = sl;
        this.voiceData = voiceData;
        this.confidence = confidence;

        if (emotion == null) {
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
        emotion = sr.getString(R.string.emotion);
        feeling = sr.getString(R.string.feeling);
    }

    /**
     * Iterate through the voice data array to see if we can match the command.
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

            String word;
            String[] wordsList;
            final Locale loc = sl.getLocale();

            String vdLower;
            int size = voiceData.size();
            for (int i = 0; i < size; i++) {
                vdLower = voiceData.get(i).toLowerCase(loc).trim();

                if (vdLower.contains(emotion) || vdLower.contains(feeling)) {

                    wordsList = vdLower.trim().split("\\s+");

                    if (wordsList.length > 5) {

                        for (int j = 0; j < 6; j++) {
                            word = wordsList[j];
                            if (word.contains(emotion) || word.contains(feeling)) {
                                toReturn.add(new Pair<>(CC.COMMAND_EMOTION, confidence[i]));
                                break;
                            }
                        }

                    } else {
                        toReturn.add(new Pair<>(CC.COMMAND_EMOTION, confidence[i]));
                    }
                }
            }
        }

        if (DEBUG) {
            MyLog.i(CLS_NAME, "emotion: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }

        return toReturn;
    }
}
