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

package ai.saiy.android.nlu.saiy;

import android.support.annotation.NonNull;

import java.util.List;

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
import ai.saiy.android.utils.UtilsString;

/**
 * Created by benrandall76@gmail.com on 03/06/2016.
 */
public class NLUSaiyHelper {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = NLUSaiyHelper.class.getSimpleName();

    /**
     * Coerce the parameters into a {@link CommandRequest} object
     *
     * @param ctx            the application context
     * @param commandRequest the {@link CommandRequest}
     * @param sl             the {@link SupportedLanguage}
     * @param entities       the parameters unique to the NLP provider
     * @return the populated {@link CommandRequest} object
     */
    public CommandRequest prepareCommand(@NonNull final android.content.Context ctx,
                                         @NonNull final CommandRequest commandRequest,
                                         @NonNull final SupportedLanguage sl,
                                         @NonNull final List<Entity> entities) {

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

                for (final Entity e : entities) {
                    if (e.getName().matches(NLUConstants.NAME_USER)) {
                        csv.setText(e.getValue());
                        csv.setStartIndex(e.getIndex()[0]);
                        csv.setEndIndex(e.getIndex()[1]);
                        commandRequest.setResolved(true);
                        break;
                    }
                }

                commandRequest.setVariableData(csv);

                break;
            case COMMAND_TRANSLATE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "prepareCommand: COMMAND_TRANSLATE");
                }

                final CommandTranslateValues ctv = new CommandTranslateValues();

                for (final Entity e : entities) {
                    if (e.getName().matches(NLUConstants.TRANSLATE_LANGUAGE)) {
                        ctv.setLanguage(e.getValue());
                        ctv.setLanguageStartIndex(e.getIndex()[0]);
                        ctv.setLanguageEndIndex(e.getIndex()[1]);
                    } else if (e.getName().matches(NLUConstants.TEXT_TO_TRANSLATE)) {
                        ctv.setText(e.getValue());
                        ctv.setTextStartIndex(e.getIndex()[0]);
                        ctv.setTextEndIndex(e.getIndex()[1]);
                    }

                    if (UtilsString.notNaked(ctv.getLanguage()) && UtilsString.notNaked(ctv.getText())) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "prepareCommand: COMMAND_TRANSLATE: extraction complete");
                        }

                        commandRequest.setResolved(true);
                        break;
                    }
                }

                commandRequest.setVariableData(ctv);

                break;
            case COMMAND_SONG_RECOGNITION:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "prepareCommand: COMMAND_SONG_RECOGNITION");
                }
                commandRequest.setResolved(true);
                break;
            case COMMAND_PARDON:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "prepareCommand: COMMAND_PARDON");
                }
                commandRequest.setResolved(true);
                break;
            case COMMAND_USER_NAME:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "prepareCommand: COMMAND_USER_NAME");
                }

                final CommandUserNameValues cunv = new CommandUserNameValues();

                for (final Entity e : entities) {
                    if (e.getName().matches(NLUConstants.NAME_USER)) {
                        cunv.setName(e.getValue());
                        cunv.setStartIndex(e.getIndex()[0]);
                        cunv.setEndIndex(e.getIndex()[1]);
                        commandRequest.setResolved(true);
                        break;
                    }
                }

                commandRequest.setVariableData(cunv);

                break;
            case COMMAND_BATTERY:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "prepareCommand: COMMAND_BATTERY");
                }

                final CommandBatteryValues cbv = new CommandBatteryValues();

                for (final Entity e : entities) {
                    if (e.getName().matches(NLUConstants.BATTERY_TYPE)) {
                        cbv.setTypeString(e.getValue());
                        cbv.setType(cbv.stringToType(ctx, sl, e.getValue()));
                        cbv.setStartIndex(e.getIndex()[0]);
                        cbv.setEndIndex(e.getIndex()[1]);
                        commandRequest.setResolved(true);
                        break;
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

                for (final Entity e : entities) {
                    if (e.getName().matches(NLUConstants.TASKER_TASK_NAME)) {
                        taskerValues.setTaskName(e.getValue());
                        taskerValues.setStartIndex(e.getIndex()[0]);
                        taskerValues.setEndIndex(e.getIndex()[1]);
                        commandRequest.setResolved(true);
                        break;
                    }
                }

                commandRequest.setVariableData(taskerValues);

                break;
            case COMMAND_WOLFRAM_ALPHA:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "prepareCommand: COMMAND_WOLFRAM_ALPHA");
                }

                final CommandWolframAlphaValues cwav = new CommandWolframAlphaValues();

                for (final Entity e : entities) {
                    if (e.getName().matches(NLUConstants.QUESTION_CONTENT)) {
                        cwav.setQuestion(e.getValue());
                        cwav.setStartIndex(e.getIndex()[0]);
                        cwav.setEndIndex(e.getIndex()[1]);
                        commandRequest.setResolved(true);
                        break;
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
