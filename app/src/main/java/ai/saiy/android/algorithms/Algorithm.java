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

package ai.saiy.android.algorithms;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Locale;

import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsString;


/**
 * Class to hold our algorithm constants and utility methods.
 * <p/>
 * Created by benrandall76@gmail.com on 21/04/2016.
 */
public enum Algorithm {

    REGEX,
    JARO_WINKLER,
    LEVENSHTEIN,
    SOUNDEX,
    METAPHONE,
    DOUBLE_METAPHONE,
    FUZZY,
    NEEDLEMAN_WUNCH,
    MONGE_ELKAN;

    public static final double JWD_LOWER_THRESHOLD = 0.88;
    public static final double JWD_UPPER_THRESHOLD = 0.93;
    public static final double JWD_MAX_THRESHOLD = 1.0;
    public static final double LEV_UPPER_THRESHOLD = 3.9;
    public static final double LEV_MAX_THRESHOLD = 0.0;
    public static final double ME_UPPER_THRESHOLD = 0.90;
    public static final double ME_MAX_THRESHOLD = 1.0;
    public static final double FUZZY_MULTIPLIER = 2.25;
    public static final double NW_UPPER_THRESHOLD = 0.85;
    public static final double NW_MAX_THRESHOLD = 1.0;
    public static final double SOUNDEX_UPPER_THRESHOLD = 2.9;
    public static final double SOUNDEX_MAX_THRESHOLD = 4;

    private static final double LENGTH_THRESHOLD = 0.75;

    /**
     * A utility method to compare the lengths of two strings and check the difference falls
     * within {@link #LENGTH_THRESHOLD}.
     *
     * @param s1 first string
     * @param s2 second string
     * @return true if the length difference falls within {@link #LENGTH_THRESHOLD}. False otherwise.
     */
    public static boolean checkLength(@NonNull final String s1, @NonNull final String s2) {
        if (s1.length() < s2.length()) {
            return ((double) s1.length() / s2.length()) > LENGTH_THRESHOLD;
        } else {
            return ((double) s2.length() / s1.length()) > LENGTH_THRESHOLD;
        }
    }

    /**
     * Get the standard {@link Algorithm} list to use dependent on the {@link SupportedLanguage}.
     * Advanced users may have selected or deselected algorithms from the standard list, so first we
     * check {@link SPH#getAlgorithms(Context)} to see if a setting has been applied.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return a list of {@link Algorithm}
     */
    public static Algorithm[] getAlgorithms(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {

        final String userAlgorithms = SPH.getAlgorithms(ctx);

        if (UtilsString.notNaked(userAlgorithms)) {
            final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            return gson.fromJson(userAlgorithms, Algorithm[].class);
        } else {

            switch (sl) {
                case ENGLISH:
                case ENGLISH_US:
                    return Algorithm.values();
            }
        }

        return Algorithm.values();
    }

    /**
     * Get the standard {@link Algorithm} list to use dependent on the {@link Locale}.
     * Advanced users may have selected or deselected algorithms from the standard list, so first we
     * check {@link SPH#getAlgorithms(Context)} to see if a setting has been applied.
     *
     * @param ctx    the application context
     * @param locale the {@link Locale}
     * @return a list of {@link Algorithm}
     */
    public static Algorithm[] getAlgorithms(@NonNull final Context ctx, @NonNull final Locale locale) {

        final String userAlgorithms = SPH.getAlgorithms(ctx);

        if (UtilsString.notNaked(userAlgorithms)) {
            final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            return gson.fromJson(userAlgorithms, Algorithm[].class);
        } else {

            if (locale.toString().toLowerCase(locale).startsWith("en")) {
                return Algorithm.values();
            }
        }

        return Algorithm.values();
    }

    /**
     * Set the standard {@link Algorithm} list to use dependent on the {@link SupportedLanguage}.
     * An advanced user has initiated this method to select or deselect algorithms from the standard
     * list containing only those supported by their {@link SPH#getAlgorithms(Context)}.
     *
     * @param ctx        the application context
     * @param algorithms a list of {@link Algorithm}
     */
    public static void setAlgorithms(@NonNull final Context ctx, @NonNull final Algorithm[] algorithms) {
        final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        SPH.setAlgorithms(ctx, gson.toJson(algorithms));
    }
}
