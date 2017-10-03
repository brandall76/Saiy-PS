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

package ai.saiy.android.command.battery;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import ai.saiy.android.command.helper.CommandRequest;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.utils.MyLog;

/**
 * Class to process a Battery request. Handles both remote NLP intents and falling back to
 * resolving locally.
 * <p>
 * Created by benrandall76@gmail.com on 10/02/2016.
 */
public class CommandBattery {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandBattery.class.getSimpleName();

    /**
     * Resolve the required command returning an {@link Outcome} object
     *
     * @param ctx       the application context
     * @param voiceData ArrayList<String> containing the voice data
     * @param sl        the {@link SupportedLanguage} we are using to analyse the voice data.
     * @param cr        the {@link CommandRequest}
     * @return {@link Outcome} containing everything we need to respond to the command.
     */
    public Outcome getResponse(@NonNull final Context ctx, @NonNull final ArrayList<String> voiceData,
                               @NonNull final SupportedLanguage sl, @NonNull final CommandRequest cr) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData.toString());
        }

        final long then = System.nanoTime();

        Outcome outcome = new Outcome();

        if (cr.isResolved()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: true");
            }

            final CommandBatteryValues cbv = (CommandBatteryValues) cr.getVariableData();
            final CommandBatteryValues.Type type = cbv.getType();
            outcome = new BatteryInformation(ctx, sl, outcome, cbv.getTypeString()).getInfo(type);
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: false");
            }
            outcome = new CommandBatteryLocal().getResponse(ctx, voiceData, sl);
        }


        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }

        return outcome;
    }
}
