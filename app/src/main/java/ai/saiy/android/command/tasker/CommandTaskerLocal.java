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

package ai.saiy.android.command.tasker;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;

/**
 * Class that uses strings to extract the Tasker Task the user wishes to execute
 * <p/>
 * Created by benrandall76@gmail.com on 10/02/2016.
 */
public class CommandTaskerLocal {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandTaskerLocal.class.getSimpleName();

    /**
     * Resolve the possible task names and return them in an ArrayList.
     *
     * @param ctx       the application context
     * @param voiceData ArrayList<String> containing the voice data
     * @param sl        the {@link SupportedLanguage} we are using to analyse the voice data.
     * @return the ArrayList of task names
     */
    public ArrayList<String> getResponse(@NonNull final Context ctx, @NonNull final ArrayList<String> voiceData,
                                         @NonNull final SupportedLanguage sl) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData.toString());
        }

        final ArrayList<String> nameData = new Tasker(sl).sort(ctx, voiceData);
        if (UtilsList.notNaked(nameData)) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "nameData: " + nameData.size() + " : " + nameData.toString());
            }
            return nameData;
        } else {
            return new ArrayList<>();
        }
    }
}
