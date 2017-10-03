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

import java.util.ArrayList;

import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;

/**
 * Class that uses strings to extract the name by which the user wants to
 * be known.
 * <p/>
 * Created by benrandall76@gmail.com on 10/02/2016.
 */
public class CommandUserNameLocal {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandUserNameLocal.class.getSimpleName();

    /**
     * Resolve the name by which the user wishes to be addressed and return it as a String.
     *
     * @param ctx       the application context
     * @param voiceData ArrayList<String> containing the voice data
     * @param sl        the {@link SupportedLanguage} we are using to analyse the voice data.
     * @return the name by which the user wishes to be addressed
     */
    public String getResponse(@NonNull final Context ctx, @NonNull final ArrayList<String> voiceData,
                              @NonNull final SupportedLanguage sl) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData.toString());
        }

        final ArrayList<String> nameData = new UserName(sl).fetch(ctx, voiceData);
        if (UtilsList.notNaked(nameData)) {
            return nameData.get(0);
        }

        return null;
    }
}
