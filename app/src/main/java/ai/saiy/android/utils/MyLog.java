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
import android.util.Log;

import java.util.concurrent.TimeUnit;

/**
 * Simple wrapper Class for debug output used across the application.
 * <p>
 * Created by benrandall76@gmail.com on 06/02/2016.
 */
public final class MyLog {

    /**
     * Prevent instantiation
     */
    public MyLog() {
        throw new IllegalArgumentException(Resources.getSystem().getString(android.R.string.no));
    }

    /* Set to false in production. All Classes check this */
    public static boolean DEBUG = true;

    private static final String TAG = "SAIY";

    public static final String DO_DEBUG = "DEBUG:";

    public static void d(@NonNull final String clsName, @NonNull final String message) {
        Log.d(TAG, clsName + ": " + message);
    }

    public static void v(@NonNull final String clsName, @NonNull final String message) {
        Log.v(TAG, clsName + ": " + message);
    }

    public static void i(@NonNull final String clsName, @NonNull final String message) {
        Log.i(TAG, clsName + ": " + message);
    }

    public static void w(@NonNull final String clsName, @NonNull final String message) {
        Log.w(TAG, clsName + ": " + message);
    }

    public static void e(@NonNull final String clsName, @NonNull final String message) {
        Log.e(TAG, clsName + ": " + message);
    }

    /**
     * Method to time the completion of code sections.
     *
     * @param clsName the class name identifier
     * @param then    the start time
     * @return the elapsed time in milliseconds.
     */
    public static long getElapsed(@NonNull final String clsName, final long then) {

        final long now = System.nanoTime();
        final long elapsed = now - then;

        if (DEBUG) {
            Log.d(TAG, clsName + ": elapsed: "
                    + TimeUnit.MILLISECONDS.convert(elapsed, TimeUnit.NANOSECONDS));
        }

        return elapsed;
    }

    /**
     * Method to time the completion of code sections.
     *
     * @param clsName    the class name identifier
     * @param additional an additional identifier
     * @param then       the start time
     * @return the elapsed time in milliseconds.
     */
    public static long getElapsed(@NonNull final String clsName, @NonNull final String additional, final long then) {

        final long now = System.nanoTime();
        final long elapsed = now - then;

        if (DEBUG) {
            Log.d(TAG, clsName + ": " + additional + ": elapsed: "
                    + TimeUnit.MILLISECONDS.convert(elapsed, TimeUnit.NANOSECONDS));
        }

        return elapsed;
    }

}
