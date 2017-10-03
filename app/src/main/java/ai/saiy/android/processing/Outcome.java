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

package ai.saiy.android.processing;

import android.support.annotation.NonNull;

import java.util.Locale;

import ai.saiy.android.service.helper.LocalRequest;

/**
 * Class that provides all possible outcomes that could be needed in {@link Quantum#onProgressUpdate(EntangledPair...)}
 * or {@link Quantum#onPostExecute(Qubit)}
 * <p>
 * Created by benrandall76@gmail.com on 09/02/2016.
 */
public class Outcome {

    public static final int SUCCESS = 0;
    public static final int FAILURE = 1;

    private String utterance;
    private int outcome = SUCCESS;
    private EntangledPair entangledPair;
    private Qubit qubit;
    private int action;
    private Locale ttsLocale;
    private Object extra;
    private int condition;

    /**
     * Get any condition that may have been applied
     *
     * @return the {@link Condition} constant
     */
    public int getCondition() {
        return condition;
    }

    /**
     * Set any required additional condition
     *
     * @param condition the {@link Condition} constant
     */
    public void setCondition(final int condition) {
        this.condition = condition;
    }

    /**
     * Get the action the outcome needs to apply
     *
     * @return one of {@link LocalRequest#ACTION_SPEAK_ONLY} or
     * {@link LocalRequest#ACTION_SPEAK_LISTEN}
     */
    public int getAction() {
        return action;
    }

    /**
     * Set the action the outcome needs to apply
     *
     * @param action one of {@link LocalRequest#ACTION_SPEAK_ONLY} or
     *               {@link LocalRequest#ACTION_SPEAK_LISTEN}
     */
    public void setAction(final int action) {
        this.action = action;
    }

    /**
     * Set the outcome of the command processing
     *
     * @param outcome one of {@link #SUCCESS} or {@link #FAILURE}
     */
    public void setOutcome(int outcome) {
        this.outcome = outcome;
    }

    /**
     * Get the outcome
     *
     * @return either {@link #SUCCESS} or {@link #FAILURE}
     */
    public int getOutcome() {
        return this.outcome;
    }

    /**
     * Set the values to be used in {@link Quantum#onProgressUpdate(EntangledPair...)}
     *
     * @param entangledPair to be used
     */
    public void setEntangledPair(@NonNull final EntangledPair entangledPair) {
        this.entangledPair = entangledPair;
    }

    /**
     * Get the values to be used in {@link Quantum#onProgressUpdate(EntangledPair...)}
     *
     * @return the values
     */
    protected EntangledPair getEntangledPair() {
        return this.entangledPair;
    }

    /**
     * Set the values to be used in {@link Quantum#onPostExecute(Qubit)}
     *
     * @param qubit to be used
     */
    public void setQubit(@NonNull final Qubit qubit) {
        this.qubit = qubit;
    }

    /**
     * Get the values to be used in {@link Quantum#onPostExecute(Qubit)}
     *
     * @return the values
     */
    public Qubit getQubit() {
        return this.qubit;
    }

    /**
     * Set the utterance to be spoken
     *
     * @param utterance to be spoken
     */
    public void setUtterance(@NonNull final String utterance) {
        this.utterance = utterance;
    }

    /**
     * Get the utterance to be spoken
     *
     * @return the string utterance
     */
    public String getUtterance() {
        return this.utterance;
    }

    /**
     * Set the custom Text to Speech {@link Locale}
     *
     * @return the custom {@link Locale}
     */
    public Locale getTTSLocale() {
        return ttsLocale;
    }

    /**
     * Get the custom Text to Speech {@link Locale}
     *
     * @param locale to set
     */
    public void setTTSLocale(@NonNull final Locale locale) {
        this.ttsLocale = locale;
    }

    /**
     * Get an object extra which can be cast to the expected type
     *
     * @return the object extra
     */
    public Object getExtra() {
        return extra;
    }

    /**
     * Set the object extra which will be cast to the expected type
     *
     * @param extra the object extra
     */
    public void setExtra(final Object extra) {
        this.extra = extra;
    }
}
