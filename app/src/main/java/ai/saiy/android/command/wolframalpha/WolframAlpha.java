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

package ai.saiy.android.command.wolframalpha;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SaiyResources;
import ai.saiy.android.localisation.SupportedLanguage;

/**
 * Helper class to direct any voice data to the correct localisation to resolve the command.
 * <p>
 * Performance is key, so initialising localised resource Strings needs to be done as few times as
 * possible, whenever possible.
 * <p>
 * Created by benrandall76@gmail.com on 06/04/2016.
 */
public class WolframAlpha implements Callable<ArrayList<Pair<CC, Float>>> {

    private final SupportedLanguage sl;
    private Object spell;

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
    public WolframAlpha(@NonNull final SaiyResources sr, @NonNull final SupportedLanguage sl,
                        @NonNull final ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = sl;

        switch (sl) {

            case ENGLISH:
                spell = new WolframAlpha_en(sr, sl, voiceData, confidence);
                break;
            case ENGLISH_US:
                spell = new WolframAlpha_en(sr, sl, voiceData, confidence);
                break;
            default:
                spell = new WolframAlpha_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    /**
     * Constructor (used during a command)
     * <p>
     * Used when we will be constructing and managing our own {@link SaiyResources} object
     *
     * @param sl the {@link SupportedLanguage}
     */
    public WolframAlpha(@NonNull final SupportedLanguage sl) {
        this.sl = sl;
    }

    /**
     * Used by the {@link Callable} in {@link Callable#call()}
     *
     * @return an array list containing {@link Pair} of {@link CC} and {@link Float} confidence scores
     */
    public ArrayList<Pair<CC, Float>> detectCallable() {

        switch (sl) {

            case ENGLISH:
                return ((WolframAlpha_en) spell).detectCallable();
            case ENGLISH_US:
                return ((WolframAlpha_en) spell).detectCallable();
            default:
                return ((WolframAlpha_en) spell).detectCallable();
        }
    }

    /**
     * Strip out all but the required command and prepare the strings to use
     *
     * @param voiceData ArrayList<String> of voice data
     * @return only the voice data associated with this command, prepared to use.
     */
    public ArrayList<String> sort(@NonNull final Context ctx, @NonNull final ArrayList<String> voiceData) {

        switch (sl) {

            case ENGLISH:
                return WolframAlpha_en.sortWolframAlpha(ctx, voiceData, sl);
            case ENGLISH_US:
                return WolframAlpha_en.sortWolframAlpha(ctx, voiceData, sl);
            default:
                return WolframAlpha_en.sortWolframAlpha(ctx, voiceData, SupportedLanguage.ENGLISH);
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
