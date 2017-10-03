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

package ai.saiy.android.nlu.apiai;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.JsonElement;

import java.util.HashMap;

import ai.saiy.android.command.battery.CommandBatteryValues;
import ai.saiy.android.command.helper.CommandRequest;
import ai.saiy.android.command.spell.CommandSpellValues;
import ai.saiy.android.command.tasker.CommandTaskerValues;
import ai.saiy.android.command.translate.CommandTranslateValues;
import ai.saiy.android.command.username.CommandUserNameValues;
import ai.saiy.android.command.wolframalpha.CommandWolframAlphaValues;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.nlu.NLUConstants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsMap;

/**
 * Created by benrandall76@gmail.com on 05/06/2016.
 */
public class NLUAPIAIHelper {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = NLUAPIAIHelper.class.getSimpleName();

    /**
     * Coerce the parameters into a {@link CommandRequest} object
     *
     * @param ctx            the application context
     * @param commandRequest the {@link CommandRequest}
     * @param sl             the {@link SupportedLanguage}
     * @param parameters     the parameters unique to the NLP provider
     * @return the populated {@link CommandRequest} object
     */
    public CommandRequest prepareCommand(@NonNull final Context ctx,
                                         @NonNull final CommandRequest commandRequest,
                                         @NonNull final SupportedLanguage sl,
                                         @NonNull final HashMap<String, JsonElement> parameters) {

        switch (commandRequest.getCC()) {

            case COMMAND_UNKNOWN:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "prepareCommand: COMMAND_UNKNOWN");
                }
                break;
            case COMMAND_CANCEL:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "prepareCommand: COMMAND_CANCEL");
                }
                break;
            case COMMAND_SPELL:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "prepareCommand: COMMAND_SPELL");
                }

                final CommandSpellValues csv = new CommandSpellValues();

                if (UtilsMap.notNaked(parameters)) {
                    if (parameters.containsKey(NLUConstants.TEXT_TO_SPELL)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "prepareCommand: COMMAND_SPELL: extraction complete");
                        }

                        csv.setText(parameters.get(NLUConstants.TEXT_TO_SPELL).getAsString());
                        commandRequest.setResolved(true);
                    }
                }

                commandRequest.setVariableData(csv);

                break;
            case COMMAND_TRANSLATE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "prepareCommand: COMMAND_TRANSLATE");
                }

                final CommandTranslateValues ctv = new CommandTranslateValues();

                if (UtilsMap.notNaked(parameters)) {
                    if (parameters.containsKey(NLUConstants.LANGUAGE)
                            && parameters.containsKey(NLUConstants.TEXT_TO_TRANSLATE)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "prepareCommand: COMMAND_TRANSLATE: extraction complete");
                        }

                        ctv.setText(parameters.get(NLUConstants.TEXT_TO_TRANSLATE).getAsString());
                        ctv.setLanguage(parameters.get(NLUConstants.LANGUAGE).getAsString());
                        commandRequest.setResolved(true);
                    }
                }

                commandRequest.setVariableData(ctv);

                break;
            case COMMAND_SONG_RECOGNITION:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "prepareCommand: COMMAND_SONG_RECOGNITION: extraction complete");
                }
                commandRequest.setResolved(true);
                break;
            case COMMAND_PARDON:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "prepareCommand: COMMAND_PARDON: extraction complete");
                }
                commandRequest.setResolved(true);
                break;
            case COMMAND_USER_NAME:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "prepareCommand: COMMAND_USER_NAME");
                }

                final CommandUserNameValues cunv = new CommandUserNameValues();

                if (UtilsMap.notNaked(parameters)) {
                    if (parameters.containsKey(NLUConstants.NAME_USER)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "prepareCommand: COMMAND_USER_NAME: extraction complete");
                        }

                        cunv.setName(parameters.get(NLUConstants.NAME_USER).getAsString());
                        commandRequest.setResolved(true);
                    }
                }

                commandRequest.setVariableData(cunv);

                break;
            case COMMAND_BATTERY:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "prepareCommand: COMMAND_BATTERY");
                }

                final CommandBatteryValues cbv = new CommandBatteryValues();

                if (UtilsMap.notNaked(parameters)) {
                    if (parameters.containsKey(NLUConstants.BATTERY_TYPE)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "prepareCommand: COMMAND_BATTERY: extraction complete");
                        }

                        cbv.setTypeString(parameters.get(NLUConstants.BATTERY_TYPE).getAsString());
                        cbv.setType(cbv.stringToType(ctx, sl, parameters.get(NLUConstants.BATTERY_TYPE)
                                .getAsString()));
                        commandRequest.setResolved(true);
                    }
                }

                commandRequest.setVariableData(cbv);

                break;
            case COMMAND_HOTWORD:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "prepareCommand: COMMAND_HOTWORD");
                }
                commandRequest.setResolved(true);
                break;
            case COMMAND_EMOTION:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "prepareCommand: COMMAND_EMOTION");
                }
                commandRequest.setResolved(true);
                break;
            case COMMAND_VOICE_IDENTIFY:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "prepareCommand: COMMAND_VOICE_IDENTIFY");
                }
                commandRequest.setResolved(true);
                break;
            case COMMAND_TASKER:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "prepareCommand: COMMAND_TASKER");
                }

                final CommandTaskerValues taskerValues = new CommandTaskerValues();

                if (UtilsMap.notNaked(parameters)) {
                    if (parameters.containsKey(NLUConstants.TASKER_TASK_NAME)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "prepareCommand: COMMAND_TASKER: extraction complete");
                        }

                        taskerValues.setTaskName(parameters.get(NLUConstants.TASKER_TASK_NAME).getAsString());
                        commandRequest.setResolved(true);
                    }
                }

                commandRequest.setVariableData(taskerValues);

                break;
            case COMMAND_WOLFRAM_ALPHA:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "prepareCommand: COMMAND_WOLFRAM_ALPHA");
                }

                final CommandWolframAlphaValues cwav = new CommandWolframAlphaValues();

                if (UtilsMap.notNaked(parameters)) {
                    if (parameters.containsKey(NLUConstants.QUESTION_CONTENT)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "prepareCommand: COMMAND_WOLFRAM_ALPHA: extraction complete");
                        }

                        cwav.setQuestion(parameters.get(NLUConstants.QUESTION_CONTENT).getAsString());
                        commandRequest.setResolved(true);
                    }
                }

                commandRequest.setVariableData(cwav);

                break;
            case COMMAND_EMPTY_ARRAY:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "prepareCommand: COMMAND_EMPTY_ARRAY");
                }
                break;
            case COMMAND_USER_CUSTOM:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "prepareCommand: COMMAND_USER_CUSTOM");
                }
                break;
            case COMMAND_SOMETHING_WEIRD:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "prepareCommand: COMMAND_SOMETHING_WEIRD");
                }
                break;
        }

        return commandRequest;
    }
}
