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

package ai.saiy.android.memory;

import android.support.annotation.NonNull;

import java.util.ArrayList;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.service.helper.LocalRequest;

/**
 * Class to structure the data of the previous request that Saiy processed. Such information will be
 * used in the {@link CC#COMMAND_PARDON} or if a voice recognition or text to speech error occurs
 * and we need to pick up from where we left off.
 * <p/>
 * Created by benrandall76@gmail.com on 20/04/2016.
 */
public class Memory {

    private final String vrLanguage;
    private final String ttsLanguage;
    private final String utterance;
    private final ArrayList<String> utteranceArray;
    private final int action;
    private final CC command;
    private final int condition;
    private final SupportedLanguage sl;

    /**
     * Constructor
     *
     * @param action         one of {@link LocalRequest#ACTION_SPEAK_LISTEN}
     *                       or {@link LocalRequest#ACTION_SPEAK_ONLY}
     * @param vrLanguage     the recognition language
     * @param ttsLanguage    the text to speech language
     * @param utterance      the utterance that Saiy spoke
     * @param utteranceArray the utterance array that Saiy spoke
     * @param command        the {@link CC} command that was performed
     * @param condition      the {@link Condition} that was applied
     * @param sl             the {@link SupportedLanguage}
     */
    public Memory(final int action, @NonNull final String vrLanguage, @NonNull final String ttsLanguage,
                  @NonNull final String utterance, @NonNull final ArrayList<String> utteranceArray,
                  @NonNull final CC command, final int condition, @NonNull final SupportedLanguage sl) {
        this.action = action;
        this.vrLanguage = vrLanguage;
        this.ttsLanguage = ttsLanguage;
        this.utterance = utterance;
        this.utteranceArray = utteranceArray;
        this.command = command;
        this.condition = condition;
        this.sl = sl;
    }

    public int getAction() {
        return action;
    }

    public CC getCommand() {
        return command;
    }

    public int getCondition() {
        return condition;
    }

    public String getVRLanguage() {
        return vrLanguage;
    }

    public SupportedLanguage getSupportedLanguage() {
        return sl;
    }

    public String getTTSLanguage() {
        return ttsLanguage;
    }

    public String getUtterance() {
        return utterance;
    }

    public ArrayList<String> getUtteranceArray() {
        return utteranceArray;
    }
}
