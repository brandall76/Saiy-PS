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

package ai.saiy.android.command.username;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Pair;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Callable;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SaiyResources;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;

/**
 * Helper class to resolve user name commands.
 * <p>
 * Created by benrandall76@gmail.com on 06/04/2016.
 */
public class UserName_en {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = UserName_en.class.getSimpleName();

    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    private static String call_me_;
    private static String address_me_as_;
    private static String addressed_as_;
    private static String my_name_is_;
    private static String be_called_;
    private static String be_known_as_;

    /**
     * Constructor
     * <p>
     * Used by a {@link Callable} to prepare everything that is need when
     * {@link Callable#call()} is executed with the {@link SaiyResources} managed elsewhere.
     *
     * @param sr         the {@link SaiyResources}
     * @param sl         the {@link SupportedLanguage}
     * @param voiceData  the array of voice data
     * @param confidence the array of confidence scores
     */
    public UserName_en(@NonNull final SaiyResources sr, @NonNull final SupportedLanguage sl,
                       @NonNull final ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = sl;
        this.voiceData = voiceData;
        this.confidence = confidence;

        if (call_me_ == null) {
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
        call_me_ = sr.getString(R.string.call_me_);
        address_me_as_ = sr.getString(R.string.address_me_as_);
        addressed_as_ = sr.getString(R.string.addressed_as_);
        my_name_is_ = sr.getString(R.string.my_name_is_);
        be_called_ = sr.getString(R.string.be_called_);
        be_known_as_ = sr.getString(R.string.be_known_as_);
    }

    /**
     * Iterate through the voice data array to see if the command can be detected.
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

                if (vdLower.contains(call_me_) || vdLower.contains(address_me_as_)
                        || vdLower.contains(my_name_is_) || vdLower.contains(addressed_as_)
                        || vdLower.contains(be_called_) || vdLower.contains(be_known_as_)) {

                    toReturn.add(new Pair<>(CC.COMMAND_USER_NAME, confidence[i]));
                }
            }
        }

        if (DEBUG) {
            MyLog.i(CLS_NAME, "userName: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }

        return toReturn;
    }

    /**
     * Static method.
     * <p>
     * Iterate through the voice data array to return only the voice data associated with the
     * required command.
     *
     * @param ctx       the application context
     * @param voiceData ArrayList<String> containing the voice data
     * @param sl        the {@link SupportedLanguage}
     * @return an array list containing only the required data
     */
    public static ArrayList<String> sortUserName(@NonNull final Context ctx, final ArrayList<String> voiceData,
                                                 @NonNull final SupportedLanguage sl) {

        final long then = System.nanoTime();
        final Locale loc = sl.getLocale();
        final ArrayList<String> toReturn = new ArrayList<>();

        if (call_me_ == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            final SaiyResources sr = new SaiyResources(ctx, sl);
            initStrings(sr);
            sr.reset();
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "strings initialised");
            }
        }

        String vdLower;
        String[] separated;
        for (final String vd : voiceData) {
            vdLower = vd.toLowerCase(loc).trim();

            if (vdLower.contains(call_me_)) {
                separated = vdLower.split(call_me_);

                if (separated.length > 1) {
                    toReturn.add(separated[1].trim());
                }
            } else if (vdLower.contains(address_me_as_)) {
                separated = vdLower.split(address_me_as_);

                if (separated.length > 1) {
                    toReturn.add(separated[1].trim());
                }
            } else if (vdLower.contains(my_name_is_)) {
                separated = vdLower.split(my_name_is_);

                if (separated.length > 1) {
                    toReturn.add(separated[1].trim());
                }
            } else if (vdLower.contains(addressed_as_)) {
                separated = vdLower.split(addressed_as_);

                if (separated.length > 1) {
                    toReturn.add(separated[1].trim());
                }
            } else if (vdLower.contains(be_called_)) {
                separated = vdLower.split(be_called_);

                if (separated.length > 1) {
                    toReturn.add(separated[1].trim());
                }
            } else if (vdLower.contains(be_known_as_)) {
                separated = vdLower.split(be_known_as_);

                if (separated.length > 1) {
                    toReturn.add(separated[1].trim());
                }
            }
        }

        if (!toReturn.isEmpty()) {
            final Set<String> deduplicated = new LinkedHashSet<>(toReturn);
            toReturn.clear();
            toReturn.addAll(deduplicated);
        }

        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }

        return toReturn;
    }
}
