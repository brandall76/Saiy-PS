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

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A collection of handy methods. Static for easy access
 * <p/>
 * Created by benrandall76@gmail.com on 07/02/2016.
 */
public class UtilsString {

    /**
     * Prevent instantiation
     */
    public UtilsString() {
        throw new IllegalArgumentException(Resources.getSystem().getString(android.R.string.no));
    }

    /**
     * Get a readable string from the {@link InputStream}
     *
     * @param is the {@link InputStream}
     * @return a readable String
     * @throws IOException
     */
    public static String streamToString(@Nullable final InputStream is) throws IOException {

        String output = "";

        if (is != null) {

            final StringBuilder stringBuilder = new StringBuilder();

            String line;

            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            bufferedReader.close();
            is.close();

            output = stringBuilder.toString();
        }

        return output;
    }

    /**
     * Utility method to check if a string is null or empty. Purely to prevent clutter wherever such
     * as check is needed.
     *
     * @param toCheck the input String
     * @return true if the String is neither null or empty
     */
    public static boolean notNaked(@Nullable final String toCheck) {
        return toCheck != null && !toCheck.trim().isEmpty();
    }

    /**
     * Remove any spaces before punctuation, left by a response failing to add in the user's name.
     *
     * @param input the input String
     * @return the stripped string
     */
    public static String stripNameSpace(@NonNull final String input) {
        return input.replaceAll(" ,", ",").replaceAll(" \\.", ".").replaceAll(" \\?", "?");
    }

    /**
     * Remove any punctuation from the beginning of utterances that might otherwise be pronounced
     * by the voice engine.
     *
     * @param input the input String
     * @return the stripped string
     */
    public static String stripLeadingPunctuation(@NonNull final String input) {
        return input.trim().matches("\\p{P}.*") ? input.trim().replaceFirst("\\p{P}", "").trim() : input.trim();
    }

    /**
     * Utility method to remove the last instance of a character from a String
     *
     * @param inputString the String to be manipulated
     * @param from        character
     * @param to          character
     * @return the manipulated String
     */
    public static String replaceLast(@NonNull final String inputString, @NonNull final String from,
                                     @NonNull final String to) {

        final int lastIndex = inputString.lastIndexOf(from);

        if (lastIndex >= 0) {
            return inputString.substring(0, lastIndex) + inputString.substring(lastIndex).replaceFirst(from, to);
        } else {
            return inputString;
        }
    }
}
