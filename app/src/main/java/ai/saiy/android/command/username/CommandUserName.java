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

import ai.saiy.android.command.helper.CommandRequest;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsString;

/**
 * Class to process a user name request. Handles both remote NLP intents and falling back to
 * resolving locally.
 * <p/>
 * Created by benrandall76@gmail.com on 10/02/2016.
 */
public class CommandUserName {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandUserName.class.getSimpleName();

    private long then;

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

        then = System.nanoTime();

        final Outcome outcome = new Outcome();

        String name;

        if (cr.isResolved()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: true");
            }

            final CommandUserNameValues cunv = (CommandUserNameValues) cr.getVariableData();
            name = cunv.getName();
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: false");
            }
            name = new CommandUserNameLocal().getResponse(ctx, voiceData, sl);
        }

        if (UtilsString.notNaked(name)) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "name: " + name);
            }

            final String response;
            final String currentName = SPH.getUserName(ctx);

            if (DEBUG) {
                MyLog.v(CLS_NAME, "currentName: " + currentName);
            }

            if (currentName.matches(name)) {
                response = String.format(PersonalityResponse.getUserNameRepeat(ctx, sl), currentName);
            } else {
                SPH.setUserName(ctx, name);
                response = String.format(PersonalityResponse.getUserName(ctx, sl), name);
                if (DEBUG) {
                    MyLog.v(CLS_NAME, "response: " + response);
                }
            }

            outcome.setUtterance(response);
            outcome.setOutcome(Outcome.SUCCESS);

        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "name naked");
            }

            name = PersonalityResponse.getUserNameError(ctx, sl);
            outcome.setUtterance(name);
            outcome.setOutcome(Outcome.FAILURE);
            return returnOutcome(outcome);
        }

        return returnOutcome(outcome);
    }

    /**
     * A single point of return to check the elapsed time for debugging.
     *
     * @param outcome the constructed {@link Outcome}
     * @return the constructed {@link Outcome}
     */
    private Outcome returnOutcome(@NonNull final Outcome outcome) {
        if (DEBUG) {
            MyLog.getElapsed(CommandUserName.class.getSimpleName(), then);
        }
        return outcome;
    }
}
