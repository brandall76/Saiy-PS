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
import android.support.annotation.NonNull;
import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SaiyResources;
import ai.saiy.android.localisation.SupportedLanguage;

/**
 * Helper class to detect the command Cancel, which is used in many places across the application,
 * not just when trying to resolve commands.
 * <p>
 * Performance is key, so initialising localised resource Strings needs to be done as few times as
 * possible, whenever possible.
 * <p>
 * Created by benrandall76@gmail.com on 06/04/2016.
 */
public class Cancel implements Callable<ArrayList<Pair<CC, Float>>> {

    private final SupportedLanguage sl;
    private final Object cancel;

    /**
     * Constructor
     * <p>
     * Used by the {@link Callable} to construct the data ready for {@link Callable#call()}
     *
     * @param sr         the {@link SaiyResources}
     * @param sl         the {@link SupportedLanguage}
     * @param voiceData  the array of voice data
     * @param confidence the array of confidence scores
     */
    public Cancel(@NonNull final SaiyResources sr, @NonNull final SupportedLanguage sl,
                  @NonNull final ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = sl;

        switch (sl) {

            case ENGLISH:
                cancel = new Cancel_en(sr, sl, voiceData, confidence);
                break;
            case ENGLISH_US:
                cancel = new Cancel_en(sr, sl, voiceData, confidence);
                break;
            default:
                cancel = new Cancel_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    /**
     * Constructor
     * <p>
     * Used when a {@link SaiyResources} object is being handled elsewhere
     *
     * @param sl    the {@link SupportedLanguage}
     * @param sr    the {@link SaiyResources}
     * @param reset set to true if the {@link SaiyResources} should be reset
     */
    public Cancel(@NonNull final SupportedLanguage sl, @NonNull final SaiyResources sr, final boolean reset) {
        this.sl = sl;

        switch (sl) {

            case ENGLISH:
                cancel = new Cancel_en(sr, reset);
                break;
            case ENGLISH_US:
                cancel = new Cancel_en(sr, reset);
                break;
            default:
                cancel = new Cancel_en(sr, reset);
                break;
        }
    }


    /**
     * Used by the {@link Callable} in {@link Callable#call()}
     *
     * @return an array list containing {@link Pair} of {@link CC} and {@link Float} confidence scores
     */
    public ArrayList<Pair<CC, Float>> detectCallable() {

        switch (sl) {

            case ENGLISH:
                return ((Cancel_en) cancel).detectCallable();
            case ENGLISH_US:
                return ((Cancel_en) cancel).detectCallable();
            default:
                return ((Cancel_en) cancel).detectCallable();
        }
    }

    /**
     * Will loop through an array to detect the command. The initialisation of any localised resources
     * will only take place once in the constructor, which is better for performance.
     * <p>
     * The language to be used is decided by the {@link SupportedLanguage} object
     *
     * @param results received from the recognition provider which will contain voice data
     * @return true if a cancel command is detected.
     */
    public boolean detectPartial(@NonNull final Bundle results) {

        switch (sl) {

            case ENGLISH:
                return ((Cancel_en) cancel).detectPartial(sl.getLocale(), results);
            case ENGLISH_US:
                return ((Cancel_en) cancel).detectPartial(sl.getLocale(), results);
            default:
                return ((Cancel_en) cancel).detectPartial(SupportedLanguage.ENGLISH.getLocale(),
                        results);
        }
    }

    /**
     * Will loop through an array to detect the command. The initialisation of any localised resources
     * will only take place once in the constructor, which is better for performance.
     * <p>
     * The language to be used is decided by the {@link SupportedLanguage} object
     *
     * @param voiceData received from the recognition provider
     * @return true if a cancel command is detected.
     */
    public boolean detectCancel(@NonNull final ArrayList<String> voiceData) {

        switch (sl) {

            case ENGLISH:
                return ((Cancel_en) cancel).detectCancel(sl.getLocale(), voiceData);
            case ENGLISH_US:
                return ((Cancel_en) cancel).detectCancel(sl.getLocale(), voiceData);
            default:
                return ((Cancel_en) cancel).detectCancel(SupportedLanguage.ENGLISH.getLocale(),
                        voiceData);
        }
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public ArrayList<Pair<CC, Float>> call() throws Exception {
        return detectCallable();
    }
}
