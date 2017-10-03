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
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SaiyResources;
import ai.saiy.android.localisation.SupportedLanguage;

/**
 * Helper class to detect the required command
 * <p>
 * Performance is key, so initialising localised resource Strings needs to be done as few times as
 * possible, whenever possible.
 * <p>
 * Created by benrandall76@gmail.com on 06/04/2016.
 */
public class UserName implements Callable<ArrayList<Pair<CC, Float>>> {

    private final SupportedLanguage sl;
    private Object userName;

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
    public UserName(@NonNull final SaiyResources sr, @NonNull final SupportedLanguage sl,
                    @NonNull final ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = sl;

        switch (sl) {

            case ENGLISH:
                userName = new UserName_en(sr, sl, voiceData, confidence);
                break;
            case ENGLISH_US:
                userName = new UserName_en(sr, sl, voiceData, confidence);
                break;
            default:
                userName = new UserName_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
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
    public UserName(@NonNull final SupportedLanguage sl) {
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
                return ((UserName_en) userName).detectCallable();
            case ENGLISH_US:
                return ((UserName_en) userName).detectCallable();
            default:
                return ((UserName_en) userName).detectCallable();
        }
    }

    /**
     * Strip out all but the required command and prepare the strings to use
     *
     * @param ctx       the application context
     * @param voiceData ArrayList<String> of voice data
     * @return only the voice data associated with this command, prepared to use.
     */
    public ArrayList<String> fetch(@NonNull final Context ctx, @NonNull final ArrayList<String> voiceData) {

        switch (sl) {

            case ENGLISH:
                return UserName_en.sortUserName(ctx, voiceData, sl);
            case ENGLISH_US:
                return UserName_en.sortUserName(ctx, voiceData, sl);
            default:
                return UserName_en.sortUserName(ctx, voiceData, SupportedLanguage.ENGLISH);
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
