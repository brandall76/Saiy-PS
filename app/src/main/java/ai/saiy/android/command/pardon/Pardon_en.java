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

package ai.saiy.android.command.pardon;

import android.support.annotation.NonNull;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SaiyResources;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;

/**
 * Helper class to resolve pardon/repeat commands.
 * <p/>
 * Created by benrandall76@gmail.com on 06/04/2016.
 */
public class Pardon_en {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = Pardon_en.class.getSimpleName();

    private static String pardon;
    private static String say_that_again;
    private static String what_did_you_say;
    private static String come_again;
    private static String repeat;
    private static String said;
    private static String that;

    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public Pardon_en(@NonNull final SaiyResources sr, @NonNull final SupportedLanguage sl,
                     @NonNull final ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = sl;
        this.voiceData = voiceData;
        this.confidence = confidence;

        if (pardon == null) {
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
        pardon = sr.getString(ai.saiy.android.R.string.pardon);
        say_that_again = sr.getString(ai.saiy.android.R.string.say_that_again);
        what_did_you_say = sr.getString(ai.saiy.android.R.string.what_did_you_say);
        come_again = sr.getString(ai.saiy.android.R.string.come_again);
        repeat = sr.getString(ai.saiy.android.R.string.repeat);
        said = sr.getString(ai.saiy.android.R.string.said);
        that = sr.getString(ai.saiy.android.R.string.that);
    }

    /**
     * Iterate through the voice data array to see if we can match the command.
     * <p>
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

                if (vdLower.startsWith(pardon)
                        || vdLower.contains(say_that_again)
                        || vdLower.contains(what_did_you_say)
                        || vdLower.startsWith(come_again)
                        || (vdLower.contains(repeat) && (vdLower.contains(said) || vdLower.contains(that)))) {
                    toReturn.add(new Pair<>(CC.COMMAND_PARDON, confidence[i]));
                }
            }
        }

        if (DEBUG) {
            MyLog.i(CLS_NAME, "pardon: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }

        return toReturn;
    }
}
