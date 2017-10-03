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

package ai.saiy.android.command.helper;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

/**
 * Helper class to prepare the parameters required to resolve a command.
 * <p/>
 * Created by benrandall76@gmail.com on 15/02/2016.
 */
public class CommandRequest {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandRequest.class.getSimpleName();

    private CC cc;
    private Object variableData;
    private boolean isResolved;
    private boolean wasSecure;
    private ArrayList<String> resultsArray;
    private float[] confidenceArray;
    private String utterance;
    private int action = LocalRequest.ACTION_SPEAK_ONLY;

    private final Locale vrLocale;
    private final Locale ttsLocale;
    private final SupportedLanguage sl;

    /**
     * @param vrLocale  the {@link Locale} of the voice recognition
     * @param ttsLocale the {@link Locale} of the text to speech engine
     */
    public CommandRequest(@NonNull final Locale vrLocale, @NonNull final Locale ttsLocale,
                          @NonNull final SupportedLanguage sl) {
        this.vrLocale = vrLocale;
        this.ttsLocale = ttsLocale;
        this.sl = sl;
    }

    /**
     * Get the SupportedLanguage
     *
     * @return the {@link SupportedLanguage}
     */
    public SupportedLanguage getSupportedLanguage() {
        return sl;
    }

    /**
     * Get the Text to Speech Locale
     *
     * @param ctx the application context
     * @return the Text to Speech {@link Locale}
     */
    public Locale getTTSLocale(@NonNull final Context ctx) {
        if (ttsLocale != null) {
            return ttsLocale;
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "ttsLocale null");
            }
        }

        return SPH.getTTSLocale(ctx);
    }

    /**
     * Get the Voice Recognition Locale
     *
     * @param ctx the application context
     * @return the Voice Recognition {@link Locale}
     */
    public Locale getVRLocale(@NonNull final Context ctx) {
        if (vrLocale != null) {
            return vrLocale;
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "Locale null");
            }
        }

        return SPH.getVRLocale(ctx);
    }

    public CC getCC() {
        return cc;
    }

    public void setCC(CC cc) {
        this.cc = cc;
    }

    /**
     * Get an Object holding further command information
     *
     * @return an Object of command information
     */
    public Object getVariableData() {
        return variableData;
    }

    /**
     * Set variable command information
     *
     * @param variableData an Object of command data
     */
    public void setVariableData(@NonNull final Object variableData) {
        this.variableData = variableData;
    }

    /**
     * Get the array of voice data
     *
     * @return the ArrayList<String> of voice data
     */
    public ArrayList<String> getResultsArray() {
        return resultsArray;
    }

    /**
     * Set the array of voice data
     *
     * @param resultsArray the array of voice data
     */
    public void setResultsArray(@NonNull final ArrayList<String> resultsArray) {
        this.resultsArray = resultsArray;
    }

    /**
     * Get the array of confidence scores associated with the voice data
     *
     * @return the float array of confidence scores
     */
    public float[] getConfidenceArray() {
        return confidenceArray;
    }

    /**
     * Set the confidence scores of the voice data
     *
     * @param confidenceArray float of scores
     */
    public void setConfidenceArray(@NonNull float[] confidenceArray) {
        this.confidenceArray = confidenceArray;
    }

    public boolean isResolved() {
        return isResolved;
    }

    public void setResolved(final boolean resolved) {
        isResolved = resolved;
    }

    /**
     * Get if the request has been made in secure mode
     *
     * @return true if the request was made in secure mode, false otherwise
     */
    public boolean wasSecure() {
        return wasSecure;
    }

    /**
     * Set the security of the command
     *
     * @param wasSecure true if the request was actioned in secure mode, false otherwise
     */
    public void setWasSecure(final boolean wasSecure) {
        this.wasSecure = wasSecure;
    }

    public static boolean inError(final CC commandInt) {

        if (commandInt != null) {

            switch (commandInt) {
                case COMMAND_UNKNOWN:
                case COMMAND_EMPTY_ARRAY:
                case COMMAND_SOMETHING_WEIRD:
                    return true;
                default:
                    return false;
            }
        }

        return false;
    }

    public String getUtterance() {
        return utterance;
    }

    public void setUtterance(@NonNull final String utterance) {
        this.utterance = utterance;
    }

    public int getAction() {
        return action;
    }

    public void setAction(final int action) {
        this.action = action;
    }
}
