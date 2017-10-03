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

package ai.saiy.android.nlu.local;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;

import ai.saiy.android.localisation.SaiyResources;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;

/**
 * Created by benrandall76@gmail.com on 09/02/2016.
 * <p/>
 * Voice recognition providers profanity filters report multiple *'s
 * to obscure the troublesome bad word....
 * <p/>
 * This helper removes entries that contain them in order
 * to avoid regex crashes when String matching later in our processing.
 * <p/>
 * The exception to the rule is when a 'calculate' command
 * may have been detected, in which case the * may denote a multiplication operation
 * and will be handled carefully elsewhere.
 * <p/>
 * Encouraging users to turn off their profanity filters isn't ideal for users who have tender ears,
 * but literal string matching across the application is so much bl**dy effort...
 */
public final class Profanity {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = Profanity.class.getSimpleName();

    private final ArrayList<String> voiceData;
    private static Pattern pCalculate;
    private static final Pattern pProfanity = Pattern.compile("[\\*]+");

    private static String calculate;

    /**
     * Constructor
     *
     * @param mContext  the application context
     * @param voiceData the array of voice data
     * @param sl        the {@link SupportedLanguage}
     */
    public Profanity(@NonNull final Context mContext, @NonNull final ArrayList<String> voiceData,
                     @NonNull final SupportedLanguage sl) {
        this.voiceData = voiceData;

        if (calculate == null || pCalculate == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            final SaiyResources sr = new SaiyResources(mContext, sl);
            initStrings(sr);
            sr.reset();
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "strings initialised");
            }
        }
    }

    private static void initStrings(@NonNull final SaiyResources sr) {
        calculate = sr.getString(ai.saiy.android.R.string.calculate);
        pCalculate = Pattern.compile(".*\\b" + calculate + "\\b.*");
    }

    /**
     * Loop through the voice data, removing any occurrences that contain the filtered profanity.
     *
     * @return the remaining entries
     */
    public ArrayList<String> remove() {

        final long then = System.nanoTime();
        final Iterator<String> itr = voiceData.iterator();

        String vd;
        while (itr.hasNext()) {
            vd = itr.next();

            if (!pCalculate.matcher(vd).find() && pProfanity.matcher(vd).find()) {
                if (DEBUG) {
                    MyLog.v(CLS_NAME, "vd removed: " + vd);
                }
                itr.remove();
            }
        }

        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }

        return voiceData;
    }
}
