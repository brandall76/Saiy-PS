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
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SaiyResources;
import ai.saiy.android.localisation.SupportedLanguage;

/**
 * Helper class to detect the required command
 * <p/>
 * Performance is key, so initialising localised resource Strings needs to be done as few times as
 * possible, whenever possible.
 * <p/>
 * Created by benrandall76@gmail.com on 06/04/2016.
 */
public class Pardon implements Callable<ArrayList<Pair<CC, Float>>> {

    private final SupportedLanguage sl;
    private final Object pardon;

    /**
     * Constructor
     * <p/>
     * Used by the {@link Callable} to construct the data ready for {@link Callable#call()}
     *
     * @param sr         the {@link SaiyResources}
     * @param sl         the {@link SupportedLanguage}
     * @param voiceData  the array of voice data
     * @param confidence the array of confidence scores
     */
    public Pardon(@NonNull final SaiyResources sr, @NonNull final SupportedLanguage sl,
                  @NonNull final ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = sl;

        switch (sl) {

            case ENGLISH:
                pardon = new Pardon_en(sr, sl, voiceData, confidence);
                break;
            case ENGLISH_US:
                pardon = new Pardon_en(sr, sl, voiceData, confidence);
                break;
            default:
                pardon = new Pardon_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
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
                return ((Pardon_en) pardon).detectCallable();
            case ENGLISH_US:
                return ((Pardon_en) pardon).detectCallable();
            default:
                return ((Pardon_en) pardon).detectCallable();
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
