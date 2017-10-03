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

package ai.saiy.android.command.emotion;

/**
 * Created by benrandall76@gmail.com on 13/08/2016.
 */

import android.content.Context;
import android.support.annotation.NonNull;

import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

/**
 * Class to process a command request. Used only to decide which introduction the user should hear.
 * <p/>
 * Created by benrandall76@gmail.com on 10/02/2016.
 */
public class CommandEmotion {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandEmotion.class.getSimpleName();

    private static final int COMMAND_EMOTION_EXTRA_VERBOSE_LIMIT = 1;
    private static final int COMMAND_EMOTION_VERBOSE_LIMIT = 2;


    /**
     * Resolve the required command returning an {@link Outcome} object
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage} we are using to analyse the voice data.
     * @return {@link Outcome} containing everything we need to respond to the command.
     */
    public Outcome getResponse(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {

        final long then = System.nanoTime();

        final Outcome outcome = new Outcome();
        outcome.setOutcome(Outcome.SUCCESS);

        switch (SPH.getEmotionCommandVerbose(ctx)) {

            case 0:
            case COMMAND_EMOTION_EXTRA_VERBOSE_LIMIT:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "COMMAND_EMOTION_EXTRA_VERBOSE_LIMIT");
                }

                outcome.setUtterance(PersonalityResponse.getBeyondVerbalExtraVerboseResponse(ctx, sl));
                SPH.incrementEmotionCommandVerbose(ctx);
                break;
            case COMMAND_EMOTION_VERBOSE_LIMIT:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "COMMAND_EMOTION_VERBOSE_LIMIT");
                }

                outcome.setUtterance(PersonalityResponse.getBeyondVerbalVerboseResponse(ctx, sl));
                SPH.incrementEmotionCommandVerbose(ctx);
                break;
            default:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Standard response");
                }

                outcome.setUtterance(PersonalityResponse.getBeyondVerbalIntroResponse(ctx, sl));
                break;
        }

        if (DEBUG) {
            MyLog.getElapsed(CommandEmotion.class.getSimpleName(), then);
        }

        return outcome;

    }
}
