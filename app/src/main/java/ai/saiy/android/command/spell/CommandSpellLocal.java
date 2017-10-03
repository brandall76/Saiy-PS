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

package ai.saiy.android.command.spell;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.processing.Qubit;
import ai.saiy.android.processing.Position;
import ai.saiy.android.processing.EntangledPair;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

/**
 * Created by benrandall76@gmail.com on 10/02/2016.
 * <p>
 * A pretty basic class to add white space between each letter of the requested spelling. Demonstrates
 * how we use UI and background threads in commands.
 * <p>
 * The spelling needs to be copied to the clipboard and 'toasted' - neither of which can be
 * performed from a background thread.
 * <p>
 * The toast is performed in onProgressUpdate and the text copied to the clipboard
 * in onPostExecute. Simples.
 */
public class CommandSpellLocal {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandSpellLocal.class.getSimpleName();

    private final Outcome outcome = new Outcome();
    private final EntangledPair entangledPair = new EntangledPair(Position.TOAST_LONG, CC.COMMAND_SPELL);

    /**
     * Resolve the required command returning an {@link Outcome} object
     *
     * @param ctx       the application context
     * @param voiceData ArrayList<String> containing the voice data
     * @param sl        the {@link SupportedLanguage} we are using to analyse the voice data.
     *                  This is not necessarily the Locale of the device, as the user may be
     *                  multi-lingual and/or have set a custom recognition language in a launcher short-cut.
     * @return {@link Outcome} containing everything we need to respond to the command.
     */
    public Outcome getResponse(@NonNull final Context ctx, @NonNull final ArrayList<String> voiceData,
                               @NonNull final SupportedLanguage sl) {

        final long then = System.nanoTime();

        final ArrayList<String> spellData = new Spell(sl).sort(ctx, voiceData);

        if (!spellData.isEmpty()) {
            setOutcomeParams(ctx, sl, spellData.get(0));
        } else {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "spell data empty");
            }

            outcome.setUtterance(PersonalityResponse.getSpellError(ctx, sl));
            outcome.setOutcome(Outcome.FAILURE);
        }

        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }

        return outcome;
    }

    /**
     * Set the parameters to the {@link Outcome}
     *
     * @param ctx     the application context
     * @param sl      the {@link SupportedLanguage}
     * @param toSpell to word to spell
     */
    private void setOutcomeParams(@NonNull final Context ctx, @NonNull final SupportedLanguage sl,
                                  @NonNull final String toSpell) {
        if (DEBUG) {
            MyLog.v(CLS_NAME, "spell: " + toSpell);
        }

        final Qubit qubit = new Qubit();
        qubit.setSpellContent(toSpell);
        outcome.setQubit(qubit);

        final String separated = getSeparated(toSpell);
        outcome.setUtterance(getResponseUtterance(ctx, sl, separated));
        entangledPair.setToastContent(separated);
        outcome.setEntangledPair(entangledPair);
        outcome.setOutcome(Outcome.SUCCESS);
    }

    /**
     * Resolve the response utterance which may or may not include a verbose explanation. This is
     * decided by how many times the user has already heard it and won't exceed
     * {@link CommandSpell#COMMAND_SPELL_VERBOSE_LIMIT}
     *
     * @param ctx     the application context
     * @param sl      the {@link SupportedLanguage}
     * @param toSpell the word to spell
     * @return the prepared response utterance
     */
    private String getResponseUtterance(@NonNull final Context ctx, @NonNull final SupportedLanguage sl,
                                        @NonNull final String toSpell) {

        if (SPH.getSpellCommandVerbose(ctx) >= CommandSpell.COMMAND_SPELL_VERBOSE_LIMIT) {
            return toSpell;
        } else {
            SPH.incrementSpellCommandVerbose(ctx);
            return toSpell + ". " + PersonalityResponse.getClipboardSpell(ctx, sl);
        }
    }

    /**
     * Add spaces between each character for toasting and the utterance
     *
     * @param toSpell the String to spell
     * @return a space separated String
     */
    private String getSeparated(@NonNull final String toSpell) {
        return toSpell.replaceAll(".(?=.)", "$0 ").trim();
    }
}
